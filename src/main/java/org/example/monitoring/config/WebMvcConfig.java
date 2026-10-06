package org.example.monitoring.config;

import org.example.monitoring.web.CurrentOperatorArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final CurrentOperatorArgumentResolver currentOperatorArgumentResolver;

    public WebMvcConfig(CurrentOperatorArgumentResolver currentOperatorArgumentResolver) {
        this.currentOperatorArgumentResolver = currentOperatorArgumentResolver;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentOperatorArgumentResolver);
    }
}
