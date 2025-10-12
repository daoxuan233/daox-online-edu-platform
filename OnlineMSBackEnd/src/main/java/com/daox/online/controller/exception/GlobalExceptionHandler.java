package com.daox.online.controller.exception;

import com.daox.online.entity.RestBean;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice // 声明为全局异常处理建言
public class GlobalExceptionHandler {
    /**
     * [优化] 处理文件上传大小超限异常 (413 Payload Too Large)。
     * @param e MaxUploadSizeExceededException 异常对象
     * @param request HttpServletRequest 请求对象
     * @return 包含 413 状态码和错误信息的 ResponseEntity
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<RestBean<Void>> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException e, HttpServletRequest request) {

        long maxSizeInMb = e.getMaxUploadSize() / 1024 / 1024;
        String message = "上传的文件过大，请确保文件大小不超过 " + maxSizeInMb + "MB。";

        log.warn("文件上传大小超限: URL={}, 允许的最大大小={}MB, 异常: {}",
                request.getRequestURI(), maxSizeInMb, e.getMessage());

        // 返回 HTTP 413 Payload Too Large
        return ResponseEntity
                .status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(RestBean.failure(HttpStatus.PAYLOAD_TOO_LARGE.value(), message));
    }

    /**
     * 处理 JSR-303 校验失败
     *
     * @param ex MethodArgumentNotValidException 异常对象
     * @return ResponseEntity 包含 RestBean 错误信息
     */
    @ExceptionHandler(MethodArgumentNotValidException.class) // 处理 JSR-303 校验失败
    public ResponseEntity<RestBean<Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.warn("请求参数校验失败: {}", errors);
        return ResponseEntity.badRequest().body(RestBean.failure(400, "请求参数无效", errors));
    }

    /**
     * 处理业务异常
     *
     * @param e BusinessException 业务异常对象
     * @return ResponseEntity 包含 RestBean 错误信息
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<RestBean<Void>> handleBusinessException(BusinessException e) {
        log.warn("发生业务异常: 错误码={}, 消息={}", e.getCode(), e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(RestBean.failure(Integer.parseInt(e.getCode()), e.getMessage()));
    }

    /**
     * 处理所有其他未捕获的异常 (500 Internal Server Error)，作为最后的防线。
     * @param e Exception 异常对象
     * @param request HttpServletRequest 请求对象
     * @return 包含 500 状态码和通用错误信息的 ResponseEntity
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<RestBean<Void>> handleGenericException(Exception e, HttpServletRequest request) {
        // 对于未知的严重错误，使用 ERROR 级别记录完整的堆栈信息
        log.error("发生未处理的系统异常: URL={}, 异常类型={}, 消息: {}",
                request.getRequestURI(), e.getClass().getName(), e.getMessage(), e);

        // 出于安全考虑，不向客户端暴露具体的服务器内部错误细节
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(RestBean.failure(HttpStatus.INTERNAL_SERVER_ERROR.value(), "服务器内部错误，请稍后重试或联系管理员。"));
    }

    /**
     * 当服务层无法找到请求的资源时（例如，根据ID查询文件或用户为空），将捕获此异常。
     * @param e ResourceNotFoundException 异常对象
     * @return 包含 404 状态码和错误信息的 ResponseEntity
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<RestBean<Void>> handleResourceNotFoundException(ResourceNotFoundException e) {
        log.warn("资源未找到: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(RestBean.failure(HttpStatus.NOT_FOUND.value(), e.getMessage()));
    }
    /**
     * 当用户认证通过，但权限不足以访问特定资源时（例如，学生试图访问未选修课程的资料），将捕获此异常。
     * @param e AccessDeniedException 异常对象
     * @return 包含 403 状态码和错误信息的 ResponseEntity
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<RestBean<Void>> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("访问被拒绝: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(RestBean.failure(HttpStatus.FORBIDDEN.value(), e.getMessage()));
    }
}
