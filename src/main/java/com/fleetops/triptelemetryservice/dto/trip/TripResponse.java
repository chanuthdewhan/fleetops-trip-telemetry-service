package com.fleetops.triptelemetryservice.dto.trip;

import com.fleetops.triptelemetryservice.entity.ProofOfDelivery;
import com.fleetops.triptelemetryservice.entity.TripEvent;
import com.fleetops.triptelemetryservice.enums.TripStatus;
import lombok.*;

import java.time.Instant;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class TripResponse {
    private String id;
    private Long orderId;
    private Long driverId;
    private Long vehicleId;
    private TripStatus status;
    private Instant startedAt;
    private Instant completedAt;
    private List<TripEvent> events;
    private ProofOfDelivery proofOfDelivery;
    private Instant createdAt;
    private Instant updatedAt;
}