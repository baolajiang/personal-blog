package com.myo.blog.utils;

import com.myo.blog.exception.ParamException;
import org.springframework.util.StringUtils;

/**
 * 参数校验工具类
 */
public class ValidationUtils {

    /**
     * 校验字符串不为空
     */
    public static void notBlank(String str, String message) {
        if (!StringUtils.hasText(str)) {
            throw new ParamException(message);
        }
    }

    /**
     * 校验对象不为空
     */
    public static void notNull(Object obj, String message) {
        if (obj == null) {
            throw new ParamException(message);
        }
    }

    /**
     * 校验数字大于0
     */
    public static void positive(Long num, String message) {
        if (num == null || num <= 0) {
            throw new ParamException(message);
        }
    }

    /**
     * 校验数字大于等于0
     */
    public static void nonNegative(Long num, String message) {
        if (num == null || num < 0) {
            throw new ParamException(message);
        }
    }
}