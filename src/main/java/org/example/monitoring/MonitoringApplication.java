package org.example.monitoring;

import org.example.monitoring.config.DotEnvLoader;
import org.example.monitoring.config.PostgresReset;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class MonitoringApplication {

    public static void main(String[] args) {
        DotEnvLoader.load(Path.of(".env"));
        List<String> springArgs = new ArrayList<>();
        boolean resetDatabase = false;
        for (String arg : args) {
            if ("-pg_reset".equals(arg)) {
                resetDatabase = true;
            } else {
                springArgs.add(arg);
            }
        }
        if (resetDatabase) {
            PostgresReset.dropAndRecreate();
        }
        SpringApplication.run(MonitoringApplication.class, springArgs.toArray(String[]::new));
    }
}
