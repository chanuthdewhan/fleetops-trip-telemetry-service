package com.fleetops.triptelemetryservice.dto.trip;

import com.fleetops.triptelemetryservice.enums.EventType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class AddEventRequest {
    @NotNull
    private EventType type;
    private Double lat;
    private Double lng;
    private String note;
}