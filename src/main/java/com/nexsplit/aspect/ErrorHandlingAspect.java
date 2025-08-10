package com.nexsplit.aspect;

import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;

@Aspect
@Component
public class ErrorHandlingAspect {
    private static final Logger logger = LoggerFactory.getLogger(ErrorHandlingAspect.class);

    @AfterThrowing(pointcut = "execution(* com.nexsplit.expense.controller..*(..))", throwing = "ex")
    public void handleControllerExceptions(Throwable ex) {
        logger.error("Error in controller: {}", ex.getMessage(), ex);
        // Note: Actual response handling is done in ControllerAdvice for HTTP responses
    }
}