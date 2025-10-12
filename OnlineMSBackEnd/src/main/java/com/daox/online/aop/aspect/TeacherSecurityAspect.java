package com.daox.online.aop.aspect;

import com.daox.online.controller.exception.ResourceNotFoundException;
import com.daox.online.entity.mysql.Assessments;
import com.daox.online.mapper.AssessmentsMapper;
import com.daox.online.uilts.UserUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;
import java.util.Objects;

@Aspect
@Component
public class TeacherSecurityAspect {
    private final AssessmentsMapper assessmentsMapper;

    public TeacherSecurityAspect(AssessmentsMapper assessmentsMapper) {
        this.assessmentsMapper = assessmentsMapper;
    }

    /**
     * 定义切点，匹配所有标注了 @RequireCourseCreator 注解的方法
     */
    @Pointcut("@annotation(com.daox.online.aop.annotation.RequireCourseCreator)")
    public void requireCourseCreatorPointcut() {}

    /**
     * 在方法执行前进行权限校验
     */
    @Before("requireCourseCreatorPointcut()")
    public void doBefore() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            // 这是一个服务器配置问题，应作为内部服务器错误处理
            throw new IllegalStateException("无法获取 HTTP 请求上下文，请检查服务器配置");
        }
        HttpServletRequest request = attributes.getRequest();

        // 1. 获取当前登录的教师 ID
        String teacherId = UserUtils.getCurrentUserId(request);
        if (teacherId == null) {
            // 抛出 AccessDeniedException，您已有的全局异常处理器会捕获并返回 403
            throw new AccessDeniedException("用户未登录，禁止访问");
        }

        // 2. 从 URL 路径变量中提取 assessmentId
        @SuppressWarnings("unchecked")
        Map<String, String> pathVariables = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        String assessmentId = pathVariables.get("assessmentId");

        if (assessmentId == null) {
            // 这通常是开发人员的错误（注解用在了不含 assessmentId 的URL上），应作为服务器错误处理
            throw new IllegalStateException("安全切面错误：无法从 URL 路径中解析出 assessmentId");
        }

        // 3. 查询数据库，校验创建者身份
        Assessments assessment = assessmentsMapper.getAssessmentById(assessmentId);
        if (assessment == null) {
            // 抛出 ResourceNotFoundException，您已有的全局异常处理器会捕获并返回 404
            throw new ResourceNotFoundException("ID 为 " + assessmentId + " 的测评不存在");
        }

        if (!Objects.equals(assessment.getCreatorId(), teacherId)) {
            // 抛出 AccessDeniedException，您已有的全局异常处理器会捕获并返回 403
            throw new AccessDeniedException("权限不足：您不是该测评的创建者，无法进行相关操作");
        }
    }
}
