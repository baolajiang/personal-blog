package com.myo.blog.common.aop;

import java.lang.annotation.*;
/**
 * 自定义注解：要求调用方必须有指定的权限才能执行方法
 * 作用：在需要权限校验的方法上添加此注解，即可触发权限校验逻辑
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {
    String value(); // 权限标识，例如 "user:list" 角色有浏览用户列表的权限
}