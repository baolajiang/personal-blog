package com.myo.blog.controller;

import com.myo.blog.common.aop.RateLimit;
import com.myo.blog.service.LoginService;
import com.myo.blog.entity.Result;
import com.myo.blog.entity.params.LoginParam;
import com.myo.blog.utils.IpUtils;
import com.myo.blog.utils.HttpContextUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("login")
public class LoginController {

    @Autowired
    private LoginService loginService;

    // 防止暴力破解，1分钟限制5次
    @RateLimit(time = 60, count = 5, msg = "账号或密码错误次数过多，请1分钟后再试")
    @PostMapping
    public Result login(@RequestBody LoginParam loginParam){
        HttpServletRequest request = HttpContextUtils.getHttpServletRequest();
        String ip = IpUtils.getIpAddr(request);
        String account = loginParam.getAccount();

        log.info("登录请求开始 - IP: {}, 账号: {}", ip, account);

        try {
            Result result = loginService.login(loginParam);

            if (result.isSuccess()) {
                log.info("登录成功 - IP: {}, 账号: {}", ip, account);
            } else {
                log.warn("登录失败 - IP: {}, 账号: {}, 错误码: {}, 错误信息: {}",
                        ip, account, result.getCode(), result.getMsg());
            }

            return result;
        } catch (Exception e) {
            log.error("登录异常 - IP: {}, 账号: {}, 异常信息: {}", ip, account, e.getMessage(), e);
            return Result.fail(500, "系统异常，请稍后重试");
        }
    }
}