package org.example.monitoring.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.time.Clock;

@Aspect
@Component
public class BudgetAspect {

    private final Clock clock;
    private final OperationTimings operationTimings;

    public BudgetAspect(Clock clock, OperationTimings operationTimings) {
        this.clock = clock;
        this.operationTimings = operationTimings;
    }

    @Pointcut("@annotation(org.example.monitoring.aop.WithinBudget)")
    void withinBudget() {
    }

    @Around("withinBudget()")
    public Object enforceBudget(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        WithinBudget budget = signature.getMethod().getAnnotation(WithinBudget.class);
        long maxMillis = budget.maxMillis();
        if (maxMillis <= 0) {
            throw new BudgetExceededException("Execution budget exceeded for " + signature.getMethod().getName());
        }
        long start = clock.millis();
        try {
            return joinPoint.proceed();
        } finally {
            long elapsed = clock.millis() - start;
            if ("findAll".equals(signature.getMethod().getName())) {
                operationTimings.recordListDuration(elapsed);
            }
            if (elapsed > maxMillis) {
                throw new BudgetExceededException(
                        "Execution budget exceeded for " + signature.getMethod().getName() + ": " + elapsed + "ms > " + maxMillis + "ms");
            }
        }
    }
}
