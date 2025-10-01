package com.myStore.myStore.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class SchedulerUtils {

    private final RestTemplate restTemplate = new RestTemplate();

    @Scheduled(cron = "0 */14 * * * *")
    public void performHealthCheck() {
        String url = "https://invoice-l7c6.onrender.com/actuator/health";
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            log.info("Health API Response: " + response.getBody());
        } catch (Exception ex) {
            log.info("Error calling health API: " + ex.getMessage());
        }
    }
}
