package com.myo.blog.handler;

import com.alibaba.fastjson.JSON;
import com.myo.blog.entity.ErrorCode;
import com.myo.blog.entity.Result;
import com.myo.blog.utils.IpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

/**
 * IP 黑名单拦截器 (修正版)
 * 功能：在请求到达 Controller 前，先检查用户 IP 是否在 Redis 黑名单中
 * 如果在，直接拒绝访问，返回自定义错误码 IP_BANNED
 */
@Component
public class IpBlackListInterceptor implements HandlerInterceptor {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 获取用户真实 IP
        String ipAddr = IpUtils.getIpAddr(request);

        // 2. 【核心修正】检查 Redis 中是否有这个 Key (BAN:IP:xxx)
        // 之前的写法 isMember("GLOBAL_IP_BLACKLIST") 是错的，因为我们现在是用独立的 Key 来存封禁信息的
        Boolean isBanned = redisTemplate.hasKey("BAN:IP:" + ipAddr);

        if (Boolean.TRUE.equals(isBanned)) {
            // 3. 如果在黑名单，拒绝访问
            response.setContentType("application/json;charset=utf-8");
            PrintWriter out = response.getWriter();

            // 使用专门定义的 IP_BANNED 错误码
            out.write(JSON.toJSONString(Result.fail(ErrorCode.IP_BANNED.getCode(), "您的IP已被系统永久封禁")));
            out.flush();
            out.close();
            return false; // 拦截成功，直接打回
        }

        // 4. 不在黑名单，放行
        return true;
    }
}