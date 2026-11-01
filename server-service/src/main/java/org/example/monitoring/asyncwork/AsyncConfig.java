package org.example.monitoring.asyncwork;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    private final MonitoringAsyncExceptionHandler exceptionHandler;
    private final ThreadPoolTaskExecutor monitoringExecutor = executor("monitoring-async-", 2, 4, 20);

    public AsyncConfig(MonitoringAsyncExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    @Bean(name = "monitoringExecutor")
    public ThreadPoolTaskExecutor monitoringExecutor() {
        return monitoringExecutor;
    }

    @Bean(name = "maintenanceExecutor")
    public ThreadPoolTaskExecutor maintenanceExecutor() {
        return executor("monitoring-maintenance-", 1, 1, 1);
    }

    @Override
    public Executor getAsyncExecutor() {
        return monitoringExecutor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return exceptionHandler;
    }

    private static ThreadPoolTaskExecutor executor(String prefix, int core, int max, int queue) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(core);
        executor.setMaxPoolSize(max);
        executor.setQueueCapacity(queue);
        executor.setThreadNamePrefix(prefix);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(false);
        return executor;
    }
}
