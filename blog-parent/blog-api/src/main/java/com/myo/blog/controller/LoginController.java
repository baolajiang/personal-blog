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
@RequestMapping("login")
public class LoginController {
//    @Autowired
//    private SysUserService sysUserService;
    @Autowired
    private LoginService loginService;
    // 防止暴力破解，1分钟限制5次
    @RateLimit(time = 60, count = 5, msg = "账号或密码错误次数过多，请1分钟后再试")
    @PostMapping
    public Result login(@RequestBody LoginParam loginParam){
        //登录 验证用户  访问用户表
        return loginService.login(loginParam);
    }
}
