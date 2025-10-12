package com.daox.ai.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class CoursesVo {
    /**
     * 课程标题
     */
    String title;
    /**
     * 课程分类
     */
    String category;
    /**
     * 课程大纲
     */
    CourseOutlineDto outline;

}
