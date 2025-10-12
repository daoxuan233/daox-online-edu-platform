package com.daox.online.aop.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解，用于标记需要校验操作者是否为测评创建者的 Controller 方法。
 * 该注解应标记在需要进行权限控制的接口方法上，且该接口的URL路径中必须包含 {assessmentId} 变量。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireCourseCreator {
}