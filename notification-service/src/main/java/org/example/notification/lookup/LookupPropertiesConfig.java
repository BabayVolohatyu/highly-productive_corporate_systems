package org.example.notification.lookup;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(GrpcServerProperties.class)
public class LookupPropertiesConfig {
}
