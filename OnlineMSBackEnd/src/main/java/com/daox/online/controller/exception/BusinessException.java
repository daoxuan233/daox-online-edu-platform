package com.daox.online.controller.exception;

import lombok.Getter;

/**
 * 业务异常 继承 RuntimeException
 */
@Getter
public class BusinessException extends RuntimeException {
    private final String code;

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }

}

