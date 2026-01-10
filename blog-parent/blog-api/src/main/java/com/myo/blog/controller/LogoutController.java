package com.myo.blog.controller;

import com.myo.blog.service.LoginService;
import com.myo.blog.entity.Result;
import com.myo.blog.utils.IpUtils;
import com.myo.blog.utils.HttpContextUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("logout")
/**
 * 退出登录控制器
 */
public class LogoutController {

    @Autowired
    private LoginService loginService;

    @GetMapping
    public Result logout(@RequestHeader("Authorization") String token){
        HttpServletRequest request = HttpContextUtils.getHttpServletRequest();
        String ip = IpUtils.getIpAddr(request);
        log.info("用户退出登录 - IP: {}, Token: {}", ip, token != null ? "已提供" : "为空");

        try {
            Result result = loginService.logout(token);
            if (result.isSuccess()) {
                log.info("退出登录成功 - IP: {}", ip);
            } else {
                log.warn("退出登录失败 - IP: {}, 错误码: {}", ip, result.getCode());
            }
            return result;
        } catch (Exception e) {
            log.error("退出登录异常 - IP: {}, 异常信息: {}", ip, e.getMessage(), e);
            return Result.fail(500, "系统异常");
        }
    }
}