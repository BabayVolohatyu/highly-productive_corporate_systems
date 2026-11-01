package org.example.monitoring.schedule;

import org.quartz.JobDataMap;

public final class JobParameters {

    private JobParameters() {
    }

    public static int requiredInt(JobDataMap data, String key) {
        if (data == null || !data.containsKey(key)) {
            throw new IllegalStateException("JobDataMap is missing " + key);
        }
        return data.getInt(key);
    }

    public static String requiredText(JobDataMap data, String key) {
        if (data == null || !data.containsKey(key) || data.getString(key) == null || data.getString(key).isBlank()) {
            throw new IllegalStateException("JobDataMap is missing " + key);
        }
        return data.getString(key);
    }
}
