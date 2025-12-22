package com.myo.blog.exception;

import com.myo.blog.entity.ErrorCode;

/**
 * 认证异常类
 */
public class AuthException extends BusinessException {
    public AuthException() {
        super(ErrorCode.NO_LOGIN);
    }

    public AuthException(String message) {
        super(ErrorCode.NO_LOGIN, message);
    }

    public AuthException(ErrorCode errorCode) {
        super(errorCode);
    }

    public AuthException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}