package com.yourcompany.batch.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties specific to Batch Job.
 * <p>
 * Properties are configured in the {@code application.yml} file.
 */
@ConfigurationProperties(prefix = "application")
public class ApplicationProperties {
    
    private Schedule schedule = new Schedule();

    public Schedule getSchedule() {
        return schedule;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    public static class Schedule {
        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}

