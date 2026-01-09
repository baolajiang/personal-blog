package com.myo.blog.common.aop;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
/**
 * 自定义限流注解
 * 用于在方法上添加限流功能，防止单位时间内请求次数超过设定的阈值。
 * 1. 可以自定义限流时间（秒）
 * 2. 可以自定义限流次数
 * 3. 可以自定义提示信息
 */
public @interface RateLimit {

    // 默认限流时间（秒）
    int time() default 60;

    // 默认限流次数
    int count() default 5;

    // 提示信息
    String msg() default "操作过于频繁，请稍后再试";
}