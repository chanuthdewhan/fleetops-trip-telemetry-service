package com.fleetops.triptelemetryservice.repository;

import com.fleetops.triptelemetryservice.entity.Trip;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TripRepository extends MongoRepository<Trip, String> {
    List<Trip> findByOrderId(Long orderId);
    Optional<Trip> findByOrderIdAndStatusNot(Long orderId, com.fleetops.triptelemetryservice.enums.TripStatus status);
}