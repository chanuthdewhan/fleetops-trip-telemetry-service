package com.fleetops.triptelemetryservice.entity;

import com.fleetops.triptelemetryservice.enums.TripStatus;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder

@Document(collection = "trips")
public class Trip {

    @Id
    private String id;

    private Long orderId;
    private Long driverId;
    private Long vehicleId;

    private TripStatus status;

    private Instant startedAt;
    private Instant completedAt;

    @Builder.Default
    private List<TripEvent> events = new ArrayList<>();

    private ProofOfDelivery proofOfDelivery;

    private Instant createdAt;
    private Instant updatedAt;
}