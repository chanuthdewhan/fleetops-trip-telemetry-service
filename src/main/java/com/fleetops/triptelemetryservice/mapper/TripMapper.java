package com.fleetops.triptelemetryservice.mapper;

import com.fleetops.triptelemetryservice.dto.trip.TripResponse;
import com.fleetops.triptelemetryservice.entity.Trip;
import org.springframework.stereotype.Component;

@Component
public class TripMapper {

    public TripResponse toResponse(Trip trip) {
        return TripResponse.builder()
                .id(trip.getId())
                .orderId(trip.getOrderId())
                .driverId(trip.getDriverId())
                .vehicleId(trip.getVehicleId())
                .status(trip.getStatus())
                .startedAt(trip.getStartedAt())
                .completedAt(trip.getCompletedAt())
                .events(trip.getEvents())
                .proofOfDelivery(trip.getProofOfDelivery())
                .createdAt(trip.getCreatedAt())
                .updatedAt(trip.getUpdatedAt())
                .build();
    }
}