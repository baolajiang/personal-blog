package com.myo.blog.controller;

import com.myo.blog.entity.ErrorCode;
import com.myo.blog.entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员接口
 * 提供封禁、解封 IP 等管理员操作
 * 1. 封禁 IP：将指定 IP 添加到全局 IP 黑名单中，拒绝其访问。
 * 2. 解封 IP：将指定 IP 从全局 IP 黑名单中移除，允许其访问。
 */
@RestController
@RequestMapping("admin")
public class AdminController {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    // 封禁 IP 接口 (记得加上管理员权限校验，别让普通人调)
    @PostMapping("ban")
    public Result banIp(@RequestParam String ip) {
        redisTemplate.opsForSet().add("GLOBAL_IP_BLACKLIST", ip);
        return Result.success("已封禁 IP: " + ip);
    }

    // 管理员手动解封
    @PostMapping("unban")
    public Result unbanIp(@RequestParam String ip) {
        Boolean result = redisTemplate.delete("BAN:IP:" + ip);
        if (Boolean.TRUE.equals(result)) {
            return Result.success("已成功解除 IP [" + ip + "] 的封禁");
        }
        return Result.fail(ErrorCode.OPERATION_FAILED.getCode(), "该 IP 未被封禁或解封失败");
    }
}
