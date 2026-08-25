package com.fleetops.triptelemetryservice.dto.trip;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class StartTripRequest {
    @NotNull
    private Long orderId;
    @NotNull
    private Long driverId;
    @NotNull
    private Long vehicleId;
}