package com.shelfpulse.activation_automation.scheduler;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class ScheduledTasks {

    // @Scheduled(cron = "0 0 * * * *")
    public void runTask() {
        // Task logic
    }
}
