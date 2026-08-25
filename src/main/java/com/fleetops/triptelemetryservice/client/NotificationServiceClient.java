package com.fleetops.triptelemetryservice.client;

import com.fleetops.triptelemetryservice.exception.NotificationServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

@Component
@Slf4j
public class NotificationServiceClient {

    private final RestClient restClient;

    public NotificationServiceClient(@LoadBalanced RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder
                .baseUrl("http://NOTIFICATION-SERVICE")
                .build();
    }

    public void notify(String type, Long referenceId, String message, String recipientRole) {
        try {
            restClient.post()
                    .uri("/api/v1/notifications")
                    .body(Map.of(
                            "type", type, "referenceId", referenceId,
                            "message", message, "recipientRole", recipientRole
                    ))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Notification call failed: {}", e.getMessage());
            throw new NotificationServiceException("Unable to send notification", e);
        }
    }
}
