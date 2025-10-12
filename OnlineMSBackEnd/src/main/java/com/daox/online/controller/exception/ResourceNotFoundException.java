package com.daox.online.controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 资源未找到异常。
 * 当根据ID或其他标识符无法找到请求的资源时抛出。
 * 此异常将由全局异常处理器捕获，并返回 HTTP 404 Not Found 状态。
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}