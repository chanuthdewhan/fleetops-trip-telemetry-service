package com.fleetops.triptelemetryservice.service.impl;

import com.fleetops.triptelemetryservice.client.NotificationServiceClient;
import com.fleetops.triptelemetryservice.client.OrderDispatchServiceClient;
import com.fleetops.triptelemetryservice.dto.trip.*;
import com.fleetops.triptelemetryservice.entity.ProofOfDelivery;
import com.fleetops.triptelemetryservice.entity.Trip;
import com.fleetops.triptelemetryservice.entity.TripEvent;
import com.fleetops.triptelemetryservice.enums.TripStatus;
import com.fleetops.triptelemetryservice.exception.InvalidStateTransitionException;
import com.fleetops.triptelemetryservice.exception.NotificationServiceException;
import com.fleetops.triptelemetryservice.exception.ResourceNotFoundException;
import com.fleetops.triptelemetryservice.mapper.TripMapper;
import com.fleetops.triptelemetryservice.repository.TripRepository;
import com.fleetops.triptelemetryservice.service.TripService;
import com.fleetops.triptelemetryservice.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TripServiceImpl implements TripService {

    private final TripRepository tripRepository;
    private final TripMapper tripMapper;
    private final FileStorageService fileStorageService;
    private final OrderDispatchServiceClient orderDispatchServiceClient;
    private final NotificationServiceClient notificationServiceClient;

    @Override
    public TripResponse startTrip(StartTripRequest request) {
        tripRepository.findByOrderIdAndStatusNot(request.getOrderId(), TripStatus.COMPLETED)
                .ifPresent(_ -> {
                    throw new InvalidStateTransitionException(
                            "An active trip already exists for order " + request.getOrderId());
                });

        Instant now = Instant.now();
        Trip trip = Trip.builder()
                .orderId(request.getOrderId())
                .driverId(request.getDriverId())
                .vehicleId(request.getVehicleId())
                .status(TripStatus.STARTED)
                .startedAt(now)
                .createdAt(now)
                .updatedAt(now)
                .build();

        Trip saved = tripRepository.save(trip);
        log.info("Trip started: id={}, orderId={}", saved.getId(), saved.getOrderId());

        notifyQuietly("TRIP_STARTED", request.getOrderId(),
                "Trip started for order #" + request.getOrderId());

        return tripMapper.toResponse(saved);
    }

    @Override
    public TripResponse addEvent(String tripId, AddEventRequest request) {
        Trip trip = getTripOrThrow(tripId);

        if (trip.getStatus() == TripStatus.COMPLETED || trip.getStatus() == TripStatus.CANCELLED) {
            throw new InvalidStateTransitionException("Cannot add events to a " + trip.getStatus() + " trip");
        }

        trip.getEvents().add(TripEvent.builder()
                .type(request.getType())
                .lat(request.getLat())
                .lng(request.getLng())
                .note(request.getNote())
                .timestamp(Instant.now())
                .build());
        trip.setStatus(TripStatus.IN_PROGRESS);
        trip.setUpdatedAt(Instant.now());

        Trip saved = tripRepository.save(trip);
        log.info("Event added to trip {}: type={}", tripId, request.getType());
        return tripMapper.toResponse(saved);
    }

    @Override
    public TripResponse uploadProofOfDelivery(String tripId, MultipartFile file) {
        Trip trip = getTripOrThrow(tripId);

        // File write happens before the DB write — if this throws, nothing was persisted yet.
        String fileUrl = fileStorageService.upload(file, "proof-of-delivery");

        trip.setProofOfDelivery(ProofOfDelivery.builder()
                .fileUrl(fileUrl)
                .uploadedAt(Instant.now())
                .build());
        trip.setUpdatedAt(Instant.now());

        Trip saved = tripRepository.save(trip);
        log.info("Proof of delivery uploaded for trip {}: {}", tripId, fileUrl);
        return tripMapper.toResponse(saved);
    }

    @Override
    public TripResponse completeTrip(String tripId) {
        Trip trip = getTripOrThrow(tripId);

        if (trip.getStatus() == TripStatus.COMPLETED) {
            throw new InvalidStateTransitionException("Trip " + tripId + " is already completed");
        }
        if (trip.getProofOfDelivery() == null) {
            throw new InvalidStateTransitionException("Cannot complete trip without proof of delivery");
        }

        // Must succeed — order status is core business state.
        // OrderDispatchServiceClient throws OrderDispatchServiceException on failure,
        // handled by GlobalExceptionHandler as 502.
        orderDispatchServiceClient.updateOrderStatus(trip.getOrderId(), "IN_TRANSIT");
        orderDispatchServiceClient.updateOrderStatus(trip.getOrderId(), "DELIVERED");

        trip.setStatus(TripStatus.COMPLETED);
        trip.setCompletedAt(Instant.now());
        trip.setUpdatedAt(Instant.now());

        Trip saved = tripRepository.save(trip);
        log.info("Trip completed: id={}, orderId={}", tripId, trip.getOrderId());

        notifyQuietly("ORDER_DELIVERED", trip.getOrderId(),
                "Order #" + trip.getOrderId() + " has been delivered");

        return tripMapper.toResponse(saved);
    }

    @Override
    public ResponseEntity<byte[]> getProofOfDeliveryFile(String tripId) {
        Trip trip = getTripOrThrow(tripId);
        if (trip.getProofOfDelivery() == null) {
            throw new ResourceNotFoundException("No proof of delivery for trip: " + tripId);
        }
        return fileStorageService.download(trip.getProofOfDelivery().getFileUrl());
    }

    @Override
    public TripResponse getTripById(String tripId) {
        return tripMapper.toResponse(getTripOrThrow(tripId));
    }

    @Override
    public List<TripResponse> getTripsByOrderId(Long orderId) {
        return tripRepository.findByOrderId(orderId).stream().map(tripMapper::toResponse).toList();
    }

    private Trip getTripOrThrow(String tripId) {
        return tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found: " + tripId));
    }

    // Notification failures are non-fatal — the trip operation already succeeded either way.
    private void notifyQuietly(String type, Long orderId, String message) {
        try {
            notificationServiceClient.notify(type, orderId, message, "DISPATCHER");
        } catch (NotificationServiceException e) {
            log.warn("Notification failed but trip operation still succeeded: {}", e.getMessage());
        }
    }
}