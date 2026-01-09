package com.myo.blog.controller;

import com.myo.blog.common.aop.RateLimit;
import com.myo.blog.service.LoginService;
import com.myo.blog.entity.Result;
import com.myo.blog.entity.params.LoginParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("register")
/**
 * 注册控制器
 */
public class RegisterController {

    @Autowired
    private LoginService loginService;

    // 1分钟1次，防止邮件轰炸
    @RateLimit(time = 60, count = 1, msg = "验证码发送太频繁，请稍后再试")
    @PostMapping("sendCode")
    public Result sendCode(@RequestBody LoginParam loginParam) {
        return loginService.sendEmailCode(loginParam.getEmail());
    }

    // 1分钟5次，防止批量注册
    @RateLimit(time = 60, count = 5, msg = "注册太频繁")
    @PostMapping
    public Result register(@RequestBody LoginParam loginParam){
        return loginService.register(loginParam);
    }
}
