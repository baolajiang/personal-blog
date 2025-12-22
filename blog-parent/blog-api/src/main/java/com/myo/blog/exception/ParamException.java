package com.myo.blog.exception;

import com.myo.blog.entity.ErrorCode;

/**
 * 参数异常类
 */
public class ParamException extends BusinessException {
    public ParamException() {
        super(ErrorCode.PARAMS_ERROR);
    }

    public ParamException(String message) {
        super(ErrorCode.PARAMS_ERROR, message);
    }
}