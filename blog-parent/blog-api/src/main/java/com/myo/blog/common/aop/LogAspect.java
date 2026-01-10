package com.myo.blog.common.aop;

import com.alibaba.fastjson.JSON;
import com.myo.blog.utils.HttpContextUtils;
import com.myo.blog.utils.IpUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.UUID;

@Component
@Aspect
@Slf4j
public class LogAspect {

    // 定义 TraceId 的 Key
    private static final String TRACE_ID = "TRACE_ID";

    @Pointcut("@annotation(com.myo.blog.common.aop.LogAnnotation)")
    public void logPointCut() {}

    @Around("logPointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long beginTime = System.currentTimeMillis();

        // 1. 生成并设置 TraceId (这是全链路监控的核心)
        String traceId = UUID.randomUUID().toString().replace("-", "");
        MDC.put(TRACE_ID, traceId);

        Object result = null;
        try {
            // 执行业务逻辑
            result = point.proceed();
        } finally {
            // 2. 无论成功失败，都记录日志，并移除 TraceId 防止内存泄漏
            long time = System.currentTimeMillis() - beginTime;
            recordLog(point, time, result);
            MDC.remove(TRACE_ID);
        }
        return result;
    }

    private void recordLog(ProceedingJoinPoint joinPoint, long time, Object result) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        LogAnnotation logAnnotation = method.getAnnotation(LogAnnotation.class);

        log.info("===================== log start =====================");

        // 3. 模块与操作描述
        log.info("module: {}", logAnnotation.module());
        log.info("operation: {}", logAnnotation.operator());

        // 4. 请求信息
        HttpServletRequest request = HttpContextUtils.getHttpServletRequest();
        log.info("url: {}", request.getRequestURL());
        log.info("method: {}.{}", signature.getDeclaringTypeName(), signature.getName());
        log.info("ip: {}", IpUtils.getIpAddr(request));

        // 5. 参数脱敏与打印 (防止密码泄露)
        Object[] args = joinPoint.getArgs();
        String params = JSON.toJSONString(args);
        // 简单脱敏：如果包含 password 字段，替换为 ******
        if (params.contains("password")) {
            params = "****** (Desensitized)";
        }
        log.info("params: {}", params);

        // 6. 响应结果截断 (防止文章列表等大数据刷屏，极大提升性能)
        String resultStr = JSON.toJSONString(result);
        if (resultStr != null && resultStr.length() > 500) {
            resultStr = resultStr.substring(0, 500) + "... (result too long, truncated)";
        }
        log.info("result: {}", resultStr);

        // 7. 性能监控与告警
        log.info("time: {} ms", time);
        if (time > 3000) {
            log.warn("🐢 [SLOW QUERY] 接口执行超过 3秒，请排查性能问题！TraceId: {}", MDC.get(TRACE_ID));
        }

        log.info("===================== log end =====================");
    }
}