package com.fleetops.triptelemetryservice.entity;

import lombok.*;
import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ProofOfDelivery {
    private String fileUrl;
    private Instant uploadedAt;
    private String notes;
}
