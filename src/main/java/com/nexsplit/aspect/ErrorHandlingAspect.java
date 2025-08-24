package com.nexsplit.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

/**
 * AOP aspect for centralized error handling and logging
 * Provides consistent error logging patterns across the application
 */
@Aspect
@Component
@Slf4j
public class ErrorHandlingAspect {

    /**
     * Handle and log controller exceptions with context
     */
    @AfterThrowing(pointcut = "execution(* com.nexsplit.controller..*(..))", throwing = "ex")
    public void handleControllerExceptions(JoinPoint joinPoint, Throwable ex) {
        String methodName = joinPoint.getSignature().toShortString();
        String className = joinPoint.getTarget().getClass().getSimpleName();


        log.error("Exception in {} method {}: {} - Stack trace: {}",
                className, methodName, ex.getMessage(), ex);
    }

    /**
     * Handle and log service exceptions with context
     */
    @AfterThrowing(pointcut = "execution(* com.nexsplit.service..*(..))", throwing = "ex")
    public void handleServiceExceptions(JoinPoint joinPoint, Throwable ex) {
        String methodName = joinPoint.getSignature().toShortString();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        log.error("Exception in {} method {}: {} - Stack trace: {}",
                className, methodName, ex.getMessage(), ex);
    }

    /**
     * Handle and log repository exceptions with context
     */
    @AfterThrowing(pointcut = "execution(* com.nexsplit.repository..*(..))", throwing = "ex")
    public void handleRepositoryExceptions(JoinPoint joinPoint, Throwable ex) {
        String methodName = joinPoint.getSignature().toShortString();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        log.error("Database exception in {} method {}: {} - Stack trace: {}",
                className, methodName, ex.getMessage(), ex);
    }
}
