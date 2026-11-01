package org.example.monitoring.asyncwork;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component
public class MonitoringAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

    private final AsyncFailureLog failureLog;

    public MonitoringAsyncExceptionHandler(AsyncFailureLog failureLog) {
        this.failureLog = failureLog;
    }

    @Override
    public void handleUncaughtException(Throwable exception, Method method, Object... params) {
        String detail = exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage();
        failureLog.record(method.getName() + ": " + detail);
    }
}
