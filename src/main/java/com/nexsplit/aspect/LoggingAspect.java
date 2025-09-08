package com.nexsplit.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;
import org.aspectj.lang.annotation.Aspect;
import com.nexsplit.util.LoggingUtil;
import com.nexsplit.util.StructuredLoggingUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * AOP aspect for centralized logging across the application
 * Provides consistent logging patterns and security-aware logging
 */
@Aspect
@Component
@Slf4j
public class LoggingAspect {

    /**
     * Log controller method entry with sanitized arguments
     * Avoids logging sensitive data like passwords, tokens
     */
    @Before("execution(* com.nexsplit.controller..*(..))")
    public void logControllerEntry(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().toShortString();
        String sanitizedArgs = sanitizeArguments(joinPoint.getArgs());
        log.debug("Entering controller method: {} with arguments: {}", methodName, sanitizedArgs);
    }

    /**
     * Log controller method exit with sanitized results
     * Avoids logging sensitive response data
     */
    //                          aspectJ pointcut expression language, to point where the advice is applied

    /*
     execution: This is the pointcut designator, indicating that the advice applies to method executions
     * : The first asterisk means any return type. The advice will apply to methods regardless of what they return (e.g., void, String, Object, etc.).
     com.nexsplit.controller: Specifies the package where the target methods reside (com.nexsplit.controller).
     .. : The double dot means any sub-package under com.nexsplit.controller. For example, it includes methods in com.nexsplit.controller, com.nexsplit.controller.auth
     * : The second asterisk means any class in the specified package or its sub-packages.
     *(..): The final part means any method in those classes, with any parameters (.. indicates zero or more parameters).

     */

    @AfterReturning(pointcut = "execution(* com.nexsplit.controller..*(..))", returning = "result")
    public void logControllerExit(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().toShortString();
        String sanitizedResult = sanitizeResult(result);
        log.debug("Exiting controller method: {} with result: {}", methodName, sanitizedResult);
    }

    /**
     * Log controller exceptions with context
     */
    @AfterThrowing(pointcut = "execution(* com.nexsplit.controller..*(..))", throwing = "ex")
    public void logControllerException(JoinPoint joinPoint, Throwable ex) {
        String methodName = joinPoint.getSignature().toShortString();
        log.error("Exception in controller method: {} - Error: {}", methodName, ex.getMessage(), ex);
    }

    /**
     * Log service method execution time and performance metrics
     */
    @Around("execution(* com.nexsplit.service..*(..))")
    public Object logServiceExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().toShortString();

        log.debug("Starting service method: {}", methodName);

        try {
            Object result = joinPoint.proceed();
            long timeTaken = System.currentTimeMillis() - startTime;

            // Use structured logging for performance events
            Map<String, Object> performanceData = new HashMap<>();
            performanceData.put("className", joinPoint.getTarget().getClass().getSimpleName());
            performanceData.put("methodSignature", joinPoint.getSignature().toString());

            StructuredLoggingUtil.logPerformanceEvent(
                    methodName,
                    timeTaken,
                    "SUCCESS",
                    performanceData);

            return result;
        } catch (Throwable ex) {
            long timeTaken = System.currentTimeMillis() - startTime;

            // Use structured logging for error events
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("className", joinPoint.getTarget().getClass().getSimpleName());
            errorData.put("methodSignature", joinPoint.getSignature().toString());
            errorData.put("durationMs", timeTaken);

            StructuredLoggingUtil.logErrorEvent(
                    "SERVICE_EXCEPTION",
                    ex.getMessage(),
                    ex.getStackTrace().toString(),
                    errorData);

            throw ex;
        }
    }

    /**
     * Sanitize method arguments to remove sensitive data
     */
    private String sanitizeArguments(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }

        StringBuilder sanitized = new StringBuilder("[");
        for (int i = 0; i < args.length; i++) {
            if (i > 0)
                sanitized.append(", ");

            Object arg = args[i];
            if (arg == null) {
                sanitized.append("null");
            } else if (isSensitiveData(arg)) {
                sanitized.append("***SENSITIVE***");
            } else {
                sanitized.append(arg.toString());
            }
        }
        sanitized.append("]");
        return sanitized.toString();
    }

    /**
     * Sanitize method results to remove sensitive data
     */
    private String sanitizeResult(Object result) {
        if (result == null) {
            return "null";
        }

        if (isSensitiveData(result)) {
            return "***SENSITIVE***";
        }

        return result.toString();
    }

    /**
     * Check if data contains sensitive information
     */
    private boolean isSensitiveData(Object obj) {
        if (obj == null)
            return false;
        return LoggingUtil.isSensitiveData(obj.toString());
    }
}
