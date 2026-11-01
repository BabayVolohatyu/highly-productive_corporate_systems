package org.example.monitoring.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.example.monitoring.server.ServerResponse;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AddressMaskAspect {

    @Pointcut("@annotation(org.example.monitoring.aop.MaskManagementAddress)")
    void maskManagementAddress() {
    }

    @Around("maskManagementAddress()")
    public Object maskAddress(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();
        if (result instanceof ServerResponse response) {
            return new ServerResponse(
                    response.id(),
                    response.hostname(),
                    maskIpv4HostOctet(response.ipAddress()),
                    response.environment(),
                    response.status(),
                    response.description(),
                    response.createdAt(),
                    response.updatedAt());
        }
        return result;
    }

    static String maskIpv4HostOctet(String ipAddress) {
        if (ipAddress == null) {
            return null;
        }
        int lastDot = ipAddress.lastIndexOf('.');
        if (lastDot > 0 && lastDot < ipAddress.length() - 1) {
            return ipAddress.substring(0, lastDot + 1) + "***";
        }
        return ipAddress;
    }
}
