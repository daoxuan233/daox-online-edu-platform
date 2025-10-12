package com.daox.online.entity.mongodb.vo;

import com.daox.online.entity.mysql.Assessments;
import com.daox.online.entity.mongodb.dto.CourseBaseDTO;
import com.daox.online.entity.mongodb.dto.paper.PaperDTO;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class AssessmentPreviewPaperVo implements Serializable {

    /**
     * 测评信息
     * @see Assessments
     */
    private Assessments assessment;

    /**
     * 课程基础信息，关联MySQL
     * @see CourseBaseDTO
     * 便于在MongoDB中直接按课程检索试卷，提高查询效率。
     */
    private CourseBaseDTO course;

    /**
     * 试卷信息
     * @see PaperDTO
     */
    private PaperDTO paper;

}
