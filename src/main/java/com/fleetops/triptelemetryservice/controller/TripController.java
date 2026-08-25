package com.fleetops.triptelemetryservice.controller;

import com.fleetops.triptelemetryservice.dto.trip.*;
import com.fleetops.triptelemetryservice.service.TripService;
import com.fleetops.triptelemetryservice.validation.ValidImage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/trips")
@RequiredArgsConstructor
@Validated
public class TripController {

    private final TripService tripService;

    @PostMapping
    public ResponseEntity<TripResponse> startTrip(@Valid @RequestBody StartTripRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tripService.startTrip(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TripResponse> getTripById(@PathVariable String id) {
        return ResponseEntity.ok(tripService.getTripById(id));
    }

    @GetMapping
    public ResponseEntity<List<TripResponse>> getTripsByOrderId(@RequestParam Long orderId) {
        return ResponseEntity.ok(tripService.getTripsByOrderId(orderId));
    }

    @PostMapping("/{id}/events")
    public ResponseEntity<TripResponse> addEvent(
            @PathVariable String id, @Valid @RequestBody AddEventRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tripService.addEvent(id, request));
    }

    @PostMapping(value = "/{id}/proof-of-delivery", consumes = "multipart/form-data")
    public ResponseEntity<TripResponse> uploadProofOfDelivery(
            @PathVariable String id,
            @RequestParam("file") @ValidImage MultipartFile file) {
        return ResponseEntity.ok(tripService.uploadProofOfDelivery(id, file));
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<TripResponse> completeTrip(@PathVariable String id) {
        return ResponseEntity.ok(tripService.completeTrip(id));
    }
}