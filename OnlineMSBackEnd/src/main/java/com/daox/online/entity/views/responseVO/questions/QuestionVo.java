package com.daox.online.entity.views.responseVO.questions;

import com.daox.online.entity.mongodb.Question;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class QuestionVo implements Serializable {
    /**
     * 课程id
     */
    private String id;
    /**
     * 课程标题
     */
    private String title;

    /**
     * 题目信息
     */
    List<Question> question;
}
