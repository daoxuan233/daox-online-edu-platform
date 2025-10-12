package com.daox.online.service;

import com.daox.online.entity.views.responseVO.course.CourseDetailVO;

public interface CourseDetailService {

    /**
     * 获取课程详情
     *
     * @param courseId 课程ID
     * @return 课程详情
     */
    CourseDetailVO getCourseDetail(String courseId);
}
