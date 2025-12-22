package com.myo.blog.handler;

import com.myo.blog.entity.Result;
import com.myo.blog.exception.BusinessException;
import com.myo.blog.exception.ParamException;
import com.myo.blog.exception.AuthException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("业务异常: {} - {} - {}", request.getRequestURI(), e.getCode(), e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数异常
     */
    @ExceptionHandler(ParamException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleParamException(ParamException e, HttpServletRequest request) {
        log.warn("参数异常: {} - {}", request.getRequestURI(), e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    /**
     * 处理认证异常
     */
    @ExceptionHandler(AuthException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result handleAuthException(AuthException e, HttpServletRequest request) {
        log.warn("认证异常: {} - {}", request.getRequestURI(), e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数校验异常 - @Validated
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数校验异常: {} - {}", request.getRequestURI(), errorMessage);
        return Result.fail(10001, "参数校验失败: " + errorMessage);
    }

    /**
     * 处理参数绑定异常 - @Valid
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleBindException(BindException e, HttpServletRequest request) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数绑定异常: {} - {}", request.getRequestURI(), errorMessage);
        return Result.fail(10001, "参数绑定失败: " + errorMessage);
    }

    /**
     * 处理约束违反异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleConstraintViolationException(ConstraintViolationException e, HttpServletRequest request) {
        String errorMessage = e.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining(", "));
        log.warn("约束违反异常: {} - {}", request.getRequestURI(), errorMessage);
        return Result.fail(10001, "参数校验失败: " + errorMessage);
    }

    /**
     * 处理其他异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常: {} - {}", request.getRequestURI(), e.getMessage(), e);
        // 生产环境可以返回更友好的错误信息
        return Result.fail(500, "系统异常，请联系管理员");
    }
}