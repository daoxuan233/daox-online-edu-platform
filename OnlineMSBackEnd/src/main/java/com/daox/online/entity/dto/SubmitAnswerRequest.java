package com.daox.online.entity.dto;

/**
 * 提交答案请求
 * @param questionId 问题ID
 * @param response 答案
 */
public record SubmitAnswerRequest(String questionId, Object response) {
}
