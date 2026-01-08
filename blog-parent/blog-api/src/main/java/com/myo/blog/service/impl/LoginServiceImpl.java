package com.myo.blog.service.impl;

import com.alibaba.fastjson.JSON;
import com.myo.blog.dao.pojo.SysUser;
import com.myo.blog.service.LoginService;
import com.myo.blog.service.SysUserService;
import com.myo.blog.utils.HttpContextUtils;
import com.myo.blog.utils.IpUtils;
import com.myo.blog.utils.JWTUtils;
import com.myo.blog.entity.ErrorCode;
import com.myo.blog.entity.Result;
import com.myo.blog.entity.params.LoginParam;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class LoginServiceImpl implements LoginService {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    // 【新增】注入邮件发送器
    @Autowired
    private JavaMailSender mailSender;

    // 从配置文件读取发件人，防止硬编码
    @Value("${spring.mail.username}")
    private String fromEmail;

    private static final String slat = "myo!@#";

    @Override
    public Result login(LoginParam loginParam) {
        String account = loginParam.getAccount();
        String password = loginParam.getPassword();
        if (StringUtils.isBlank(account) || StringUtils.isBlank(password)){
            return Result.fail(ErrorCode.PARAMS_ERROR.getCode(),ErrorCode.PARAMS_ERROR.getMsg());
        }
        password = DigestUtils.md5Hex(password + slat);

        SysUser sysUser = sysUserService.findUser(account,password);
        if (sysUser == null){
            return Result.fail(ErrorCode.ACCOUNT_PWD_NOT_EXIST.getCode(),ErrorCode.ACCOUNT_PWD_NOT_EXIST.getMsg());
        }

        String token = JWTUtils.createToken(sysUser.getId());
        //更新最后登录IP,并设置登录时间
        updateLoginInfo(sysUser.getId());
        saveTokenToRedis(token, sysUser);
        return Result.success(token);
    }

    public void updateLoginInfo(Long userId){
        HttpServletRequest request = HttpContextUtils.getHttpServletRequest();
        String ip= IpUtils.getIpAddr(request);
        SysUser user = new SysUser();
        user.setId(userId);
        user.setLastIpaddr(ip);
        user.setLastLogin(System.currentTimeMillis());
        this.sysUserService.updateById(user);
    }

    @Override
    public SysUser checkToken(String token) {
        if (StringUtils.isBlank(token)){
            return null;
        }
        Map<String, Object> stringObjectMap = JWTUtils.checkToken(token);
        if (stringObjectMap == null){
            return null;
        }
        String userJson = redisTemplate.opsForValue().get("TOKEN_" + token);
        if (StringUtils.isBlank(userJson)){
            return null;
        }
        SysUser sysUser = JSON.parseObject(userJson, SysUser.class);
        redisTemplate.expire("TOKEN_" + token, 3, TimeUnit.DAYS);
        redisTemplate.expire("USER_TOKEN_" + sysUser.getId(), 3, TimeUnit.DAYS);
        return sysUser;
    }

    @Override
    public Result logout(String token) {
        SysUser sysUser = checkToken(token);
        redisTemplate.delete("TOKEN_" + token);
        if (sysUser != null) {
            redisTemplate.delete("USER_TOKEN_" + sysUser.getId());
        }
        return Result.success(null);
    }

    // ================== 【核心修改区域：发送验证码】 ==================

    public Result sendEmailCode(String email) {
        if (StringUtils.isBlank(email)) {
            return Result.fail(ErrorCode.PARAMS_ERROR.getCode(), "邮箱不能为空");
        }

        // 1. 生成 6 位随机数
        String code = String.valueOf(new Random().nextInt(899999) + 100000);

        // 2. 发送邮件
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail); // 发件人
            message.setTo(email);       // 收件人
            message.setSubject("【月之别邸】注册验证码");
            message.setText("欢迎来到月之别邸，您的注册验证码是：" + code + "。有效期 5 分钟，请勿泄露。");
            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail(999, "邮件发送失败，请检查邮箱地址");
        }

        // 3. 存入 Redis，Key = REGISTER_CODE_邮箱，有效期 5 分钟
        redisTemplate.opsForValue().set("REGISTER_CODE_" + email, code, 5, TimeUnit.MINUTES);

        return Result.success("验证码发送成功");
    }

    // ================== 【核心修改区域：注册逻辑】 ==================
    @Override
    public Result register(LoginParam loginParam) {
        String account = loginParam.getAccount();
        String password = loginParam.getPassword();
        String nickname = loginParam.getNickname();
        String email = loginParam.getEmail();
        String code = loginParam.getCode(); // 前端传来的验证码

        // 1. 基础校验
        if (StringUtils.isBlank(account)
                || StringUtils.isBlank(password)
                || StringUtils.isBlank(nickname)
                || StringUtils.isBlank(email)
                || StringUtils.isBlank(code) // 必填验证码
        ){
            return Result.fail(ErrorCode.PARAMS_ERROR.getCode(),ErrorCode.PARAMS_ERROR.getMsg());
        }

        // 2. 【核心】校验验证码
        String redisCode = redisTemplate.opsForValue().get("REGISTER_CODE_" + email);
        // 如果 Redis 里没有，说明过期了或者根本没发
        if (StringUtils.isBlank(redisCode)) {
            return Result.fail(ErrorCode.PARAMS_ERROR.getCode(), "验证码已过期或未获取");
        }
        // 比对验证码
        if (!redisCode.equals(code)) {
            return Result.fail(ErrorCode.PARAMS_ERROR.getCode(), "验证码错误");
        }

        // 3. 校验账号是否已存在
        SysUser sysUser = sysUserService.findUserByAccount(account);
        if (sysUser != null){
            return Result.fail(ErrorCode.ACCOUNT_EXIST.getCode(),"账户已经被注册了");
        }

        // 4. 执行注册入库
        sysUser = new SysUser();
        sysUser.setNickname(nickname);
        sysUser.setAccount(account);
        sysUser.setPassword(DigestUtils.md5Hex(password+slat));
        sysUser.setCreateDate(System.currentTimeMillis());
        sysUser.setLastLogin(System.currentTimeMillis());
        sysUser.setAvatar("/static/img/tx.gif");
        sysUser.setAdmin(0);
        sysUser.setDeleted(0);
        sysUser.setSalt("");
        sysUser.setStatus("");
        sysUser.setEmail(email);

        HttpServletRequest request = HttpContextUtils.getHttpServletRequest();
        String ip= IpUtils.getIpAddr(request);
        sysUser.setIpaddr(ip);
        sysUser.setLastIpaddr("");
        this.sysUserService.save(sysUser);

        // 5. 注册成功后，删除验证码（防止二次使用）
        redisTemplate.delete("REGISTER_CODE_" + email);

        // 6. 自动登录
        String token = JWTUtils.createToken(sysUser.getId());
        saveTokenToRedis(token, sysUser);

        return Result.success(token);
    }

    @Override
    public Result kick(Long userId) {
        String token = redisTemplate.opsForValue().get("USER_TOKEN_" + userId);
        if (StringUtils.isBlank(token)) {
            return Result.fail(ErrorCode.NO_LOGIN.getCode(), "该用户未登录或已下线");
        }
        redisTemplate.delete("TOKEN_" + token);
        redisTemplate.delete("USER_TOKEN_" + userId);
        return Result.success("用户已强制下线");
    }

    private void saveTokenToRedis(String token, SysUser sysUser) {
        redisTemplate.opsForValue().set("TOKEN_" + token, JSON.toJSONString(sysUser), 3, TimeUnit.DAYS);
        redisTemplate.opsForValue().set("USER_TOKEN_" + sysUser.getId(), token, 3, TimeUnit.DAYS);
    }
}