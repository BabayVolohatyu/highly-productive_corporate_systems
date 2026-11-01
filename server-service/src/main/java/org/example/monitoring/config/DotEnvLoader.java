package org.example.monitoring.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class DotEnvLoader {

    private DotEnvLoader() {
    }

    public static void load(Path path) {
        if (!Files.exists(path)) {
            return;
        }
        try {
            for (String line : Files.readAllLines(path)) {
                apply(line);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read " + path.toAbsolutePath(), exception);
        }
    }

    private static void apply(String line) {
        String trimmed = line.trim();
        if (trimmed.isEmpty() || trimmed.startsWith("#")) {
            return;
        }
        if (trimmed.startsWith("export ")) {
            trimmed = trimmed.substring("export ".length()).trim();
        }
        int separator = trimmed.indexOf('=');
        if (separator < 1) {
            return;
        }
        String key = trimmed.substring(0, separator).trim();
        String value = stripQuotes(trimmed.substring(separator + 1).trim());
        if (System.getenv(key) == null && System.getProperty(key) == null) {
            System.setProperty(key, value);
        }
    }

    private static String stripQuotes(String value) {
        if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);
        }
        if (value.length() >= 2 && value.startsWith("'") && value.endsWith("'")) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }
}
