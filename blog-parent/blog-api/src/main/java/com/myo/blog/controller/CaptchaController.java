package com.myo.blog.controller;

import com.myo.blog.entity.Result;
import com.myo.blog.entity.params.CaptchaParam;
import com.wf.captcha.SpecCaptcha;
import com.wf.captcha.base.Captcha;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

// 注意：已删除 javax.servlet 的导入，因为代码中并没有实际使用它们
// 如果将来需要用到 Request/Response，请引入 jakarta.servlet.*

/**
 * Created by IntelliJ IDEA.
 *
 * @Author : myo
 * @create 2023/8/3 21:39
 */
@RestController
@RequestMapping("yzm")
public class CaptchaController {

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @PostMapping("/captchaClass")
    public Result captchaClass() throws Exception {
        // easy-captcha 1.6.2 可能在某些环境下会有字体问题，但在 base64 模式下通常可用
        SpecCaptcha specCaptcha = new SpecCaptcha(130, 48, 5);
        /**
         * TYPE_DEFAULT	数字和字母混合
         * TYPE_ONLY_NUMBER	纯数字
         * TYPE_ONLY_CHAR	纯字母
         * TYPE_ONLY_UPPER	纯大写字母
         * TYPE_ONLY_LOWER	纯小写字母
         * TYPE_NUM_AND_UPPER	数字和大写字母
         * **/
        specCaptcha.setCharType(Captcha.TYPE_DEFAULT);
        String verCode = specCaptcha.text().toLowerCase();
        String key = "yzm_"+verCode;

        // 存入redis并设置过期时间为2分钟
        redisTemplate.opsForValue().set(key, verCode, 2, TimeUnit.MINUTES);

        Map<String, Object> map = new HashMap<>();
        map.put("key", key);
        map.put("image", specCaptcha.toBase64());
        return Result.success(map);
    }

    @PostMapping("/login")
    public Result login(@RequestBody CaptchaParam captcha){
        String verKey = captcha.getVerKey();
        String verCode = captcha.getVerCode();
        // 获取redis中的验证码
        String redisCode = redisTemplate.opsForValue().get(verKey);

        if(redisCode == null){
            return Result.success("验证码已过期");
        }

        if (verCode == null || !redisCode.equals(verCode.trim().toLowerCase())) {
            return Result.success("验证码不正确");
        }

        // 验证成功，删除验证码
        redisTemplate.delete("yzm_" + redisCode); // 这里的逻辑原来的代码似乎有点奇怪，通常应该删 verKey
        // 原逻辑是删除 "yzm_"+redisCode，假设 key 就是这个格式。
        // 但上面的 key 是 "yzm_" + verCode。
        // 如果 verKey 传进来的是 key (例如 "yzm_abcde")，那么这里 delete(verKey) 更合理。
        // 按照您原有的逻辑保留，但建议确认一下删除逻辑是否符合预期。

        return Result.success("验证码正确");
    }
}