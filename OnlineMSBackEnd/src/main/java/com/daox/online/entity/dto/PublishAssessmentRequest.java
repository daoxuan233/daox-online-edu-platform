package com.daox.online.entity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 发布测评请求DTO
 * 用于接收前端发送的发布测评请求参数
 * 
 * @author DaoX
 * @version 1.0
 * @since 2024-01-01
 */
@Data
public class PublishAssessmentRequest implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 测评ID
     * 要发布的测评的唯一标识符
     */
    private String assessmentId;
    
    /**
     * 默认构造函数
     */
    public PublishAssessmentRequest() {
    }
    
    /**
     * 带参数的构造函数
     * 
     * @param assessmentId 测评ID
     */
    public PublishAssessmentRequest(String assessmentId) {
        this.assessmentId = assessmentId;
    }
}