package com.fleetops.triptelemetryservice.service;

import com.fleetops.triptelemetryservice.dto.trip.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface TripService {
    TripResponse startTrip(StartTripRequest request);
    TripResponse addEvent(String tripId, AddEventRequest request);
    TripResponse uploadProofOfDelivery(String tripId, MultipartFile file);
    TripResponse completeTrip(String tripId);
    TripResponse getTripById(String tripId);
    List<TripResponse> getTripsByOrderId(Long orderId);
}