package com.daox.online.controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 访问被拒绝异常。
 * 当用户尝试访问他们没有权限的资源时抛出。
 * 此异常将由全局异常处理器捕获，并返回 HTTP 403 Forbidden 状态。
 */
@ResponseStatus(HttpStatus.FORBIDDEN) // 建议添加，为异常赋予语义化的HTTP状态
public class AccessDeniedException extends RuntimeException {
    public AccessDeniedException(String message) {
        super(message);
    }
}