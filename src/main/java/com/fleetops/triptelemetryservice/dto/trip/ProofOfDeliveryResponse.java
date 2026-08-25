package com.fleetops.triptelemetryservice.dto.trip;

import lombok.*;
import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ProofOfDeliveryResponse {
    private String fileUrl;
    private Instant uploadedAt;
}