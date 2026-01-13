package com.myo.blog.common.aop;

import com.myo.blog.dao.mapper.SysUserMapper;
import com.myo.blog.dao.pojo.SysUser;
import com.myo.blog.entity.ErrorCode;
import com.myo.blog.entity.Result;
import com.myo.blog.utils.UserThreadLocal;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 权限切面类
 * 作用：拦截带有 @RequirePermission 注解的方法，进行权限校验
 */
@Aspect     // 1. 声明这是一个切面类
@Component  // 2. 将该类交给 Spring 容器管理（实例化为 Bean）
public class PermissionAspect {

    @Autowired
    private SysUserMapper sysUserMapper; // 注入 Mapper，用于去数据库查用户的权限列表

    /**
     * 定义切点 (Pointcut)
     * 意思就是：瞄准所有头上贴了 @RequirePermission 注解的方法
     * 只要方法上有这个注解，就会触发下面的 around 逻辑
     */
    @Pointcut("@annotation(com.myo.blog.common.aop.RequirePermission)")
    public void permissionPointCut() {}

    /**
     * 环绕通知 (Around Advice)
     * 这是真正干活的地方！它会在目标方法执行【之前】运行。
     *
     * @param point             连接点，代表那个被拦截的方法（可以控制它是否执行）
     * @param requirePermission 注解对象，从中可以拿到方法要求的权限值（比如 "user:delete"）
     * @return 目标方法的返回值（或者拦截后的错误信息）
     */
    @Around("permissionPointCut() && @annotation(requirePermission)")
    public Object around(ProceedingJoinPoint point, RequirePermission requirePermission) throws Throwable {

        // ============================================================
        // 第一步：查户口（获取当前是谁在操作）
        // ============================================================
        // 从 ThreadLocal 获取当前登录用户（这个是在 LoginInterceptor 里存进去的）
        SysUser sysUser = UserThreadLocal.get();
        if (sysUser == null) {
            // 如果拿不到用户，说明没登录或 Token 过期，直接拦截，不让进
            return Result.fail(ErrorCode.NO_LOGIN.getCode(), "未登录");
        }

        // ============================================================
        // 第二步：开后门（超级管理员特权）
        // ============================================================
        // 如果是站长/超管（admin=1），就像拿了万能钥匙，直接放行，不需要查具体权限表
        // 这是一个性能优化，也是防止误操作把自己锁在门外
        if (sysUser.getAdmin() != null && sysUser.getAdmin() == 1) {
            return point.proceed(); // 【放行】：程序继续执行 Controller 里的业务代码
        }

        // ============================================================
        // 第三步：看门锁（获取该接口需要的权限）
        // ============================================================
        // 读取 Controller 方法上注解里的值，比如 @RequirePermission("user:ban") -> 拿到 "user:ban"
        String requiredPermission = requirePermission.value();

        // ============================================================
        // 第四步：查钥匙（去数据库查当前用户有哪些权限）
        // ============================================================
        // 执行 SQL：SELECT code FROM ... WHERE user_id = ?
        // 这一步会查询关联表：用户 -> 角色 -> 权限
        List<String> userPermissions = sysUserMapper.findPermissionsByUserId(sysUser.getId());

        // ============================================================
        // 第五步：比对（钥匙能开锁吗？）
        // ============================================================
        // 判断用户拥有的权限列表里，是否包含接口要求的那个权限
        if (userPermissions.contains(requiredPermission)) {
            // 匹配成功！有权限
            return point.proceed(); // 【放行】：允许执行业务逻辑
        } else {
            // 匹配失败！无权限
            // 直接返回错误结果，Controller 方法根本不会被执行
            return Result.fail(ErrorCode.NO_PERMISSION.getCode(), "无权访问");
        }
    }
}