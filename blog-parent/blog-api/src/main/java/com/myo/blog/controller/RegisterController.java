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
public class RegisterController {

    @Autowired
    private LoginService loginService;
    //限制 60 秒内只能请求 1 次
    @RateLimit(time = 60, count = 1, msg = "验证码发送太频繁，请1分钟后再试")
    @PostMapping
    public Result register(@RequestBody LoginParam loginParam){

        //sso 单点登录，后期如果把登录注册功能 提出去（单独的服务，可以独立提供接口服务）
        return loginService.register(loginParam);
    }
    //限制 60 秒内只能尝试注册 2 次
    @RateLimit(time = 60, count = 2, msg = "注册尝试次数过多")
    @PostMapping("sendCode")
    public Result sendCode(@RequestBody LoginParam loginParam) {
        return loginService.sendEmailCode(loginParam.getEmail());
    }

}
