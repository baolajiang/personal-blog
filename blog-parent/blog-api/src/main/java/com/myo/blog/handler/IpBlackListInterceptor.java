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
 * IP 黑名单拦截器
 * 用于检查用户请求的 IP 是否在全局 IP 黑名单中。
 * 如果在黑名单，直接拒绝访问，返回 JSON 提示。
 * 1. 从请求中获取用户真实 IP。WebMVCConfig
 * 2. 检查 Redis 中是否有这个 IP（假设黑名单 Set 的 Key 是 "GLOBAL_IP_BLACKLIST"）。
 * 3. 如果在黑名单，拒绝访问，返回 JSON 提示。
 * 4. 如果不在黑名单，放行。
 */
@Component
public class IpBlackListInterceptor implements HandlerInterceptor {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 获取用户真实 IP
        String ipAddr = IpUtils.getIpAddr(request);

        // 2. 检查 Redis 中是否有这个 IP (假设黑名单 Set 的 Key 是 "GLOBAL_IP_BLACKLIST")
        Boolean isBanned = redisTemplate.opsForSet().isMember("GLOBAL_IP_BLACKLIST", ipAddr);

        if (Boolean.TRUE.equals(isBanned)) {
            // 3. 如果在黑名单，直接返回 JSON 拒绝访问
            response.setContentType("application/json;charset=utf-8");
            PrintWriter out = response.getWriter();
            out.write(JSON.toJSONString(Result.fail(ErrorCode.NO_PERMISSION.getCode(), "您的IP已被系统永久封禁")));
            out.flush();
            out.close();
            return false; // 拦截成功，不再往下执行
        }

        // 4. 不在黑名单，放行
        return true;
    }
}