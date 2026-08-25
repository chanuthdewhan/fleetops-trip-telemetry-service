package com.fleetops.triptelemetryservice.client;

import com.fleetops.triptelemetryservice.exception.OrderDispatchServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

@Component
@Slf4j
public class OrderDispatchServiceClient {

    private final RestClient restClient;

    public OrderDispatchServiceClient(@LoadBalanced RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder
                .baseUrl("http://ORDER-DISPATCH-SERVICE")
                .build();
    }

    public void updateOrderStatus(Long orderId, String status) {
        log.debug("Updating order {} status to {}", orderId, status);
        try {
            restClient.patch()
                    .uri("/api/v1/orders/{id}/status", orderId)
                    .body(Map.of("status", status))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            log.error("Failed to update order {} status to {}", orderId, status, e);
            throw new OrderDispatchServiceException("Unable to update status for order: " + orderId, e);
        }
    }
}