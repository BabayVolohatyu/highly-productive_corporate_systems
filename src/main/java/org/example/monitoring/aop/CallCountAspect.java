package org.example.monitoring.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class CallCountAspect {

    private final CallMeter callMeter;

    public CallCountAspect(CallMeter callMeter) {
        this.callMeter = callMeter;
    }

    @Pointcut("@annotation(org.example.monitoring.aop.CountExternalCalls)")
    void countedExternalCall() {
    }

    @Before("countedExternalCall()")
    public void countExternalCall(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        callMeter.recordExternalCall(signature.getMethod().getName());
    }
}
