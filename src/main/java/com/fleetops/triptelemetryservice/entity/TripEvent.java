package com.fleetops.triptelemetryservice.entity;

import com.fleetops.triptelemetryservice.enums.EventType;
import lombok.*;

import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class TripEvent {
    private EventType type;
    private Double lat;
    private Double lng;
    private String note;
    private Instant timestamp;
}
