package com.daox.online.entity.views.requestDTO;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 提交答案请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmitAnswerDTO {
    
    /**
     * 题目ID
     */
    private String questionId;
    
    /**
     * 学生答案内容
     */
    private Object answer;
}