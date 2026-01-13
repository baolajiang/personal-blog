package com.myo.blog.handler;

import com.alibaba.fastjson.JSON;
import com.myo.blog.dao.pojo.SysUser;
import com.myo.blog.entity.ErrorCode;
import com.myo.blog.entity.Result;
import com.myo.blog.utils.UserThreadLocal;
import lombok.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
// 管理员拦截器
@Component
public class AdminInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        // 1. 如果不是映射到Controller方法（例如静态资源），直接放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        // 2. 获取当前登录用户
        // 注意：AdminInterceptor 必须配置在 LoginInterceptor 之后执行
        // 这样 UserThreadLocal 里才会有数据
        SysUser sysUser = UserThreadLocal.get();

        // 3. 防御性编程：虽然理论上 LoginInterceptor 已经拦过空用户了，但多判断一次更安全
        if (sysUser == null) {
            return sendError(response, ErrorCode.NO_LOGIN.getCode(), "未登录");
        }

        // 4. 【核心】判断是否是管理员
        // 假设 1 代表管理员，其他都是普通用户
        if (sysUser.getAdmin() == null || sysUser.getAdmin() != 1) {
            return sendError(response, ErrorCode.NO_PERMISSION.getCode(), "无权访问，需要管理员权限");
        }

        // 5. 是管理员，放行
        return true;
    }

    // 辅助方法：返回 JSON 格式的错误信息
    private boolean sendError(HttpServletResponse response, int code, String msg) throws Exception {
        Result result = Result.fail(code, msg);
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().print(JSON.toJSONString(result));
        return false;
    }
}