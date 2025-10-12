package com.daox.ai.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Component
@Aspect
@Slf4j
public class LogAspect {

    /**
     * 记录执行时间
     *
     * @param joinPoint 切入点
     * @return 方法执行结果
     * @throws Throwable 抛出异常
     */
    @Around("execution(* com.daox.ai.service.Impl..*.*(..))")
    public Object logTime(ProceedingJoinPoint joinPoint) throws Throwable {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        Object proceed = joinPoint.proceed();
        // 获取执行名称
        String methodName = joinPoint.getTarget().getClass() + "." + joinPoint.getSignature().getName();
        stopWatch.stop();
        log.info("{} 执行耗时: {}ms", methodName, stopWatch.getTotalTimeMillis());
        return proceed;
    }
}
