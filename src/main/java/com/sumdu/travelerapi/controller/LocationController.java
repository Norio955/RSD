package com.sumdu.travelerapi.controller;

import com.sumdu.travelerapi.model.Location;
import com.sumdu.travelerapi.repository.LocationRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    private final LocationRepository locationRepository;

    public LocationController(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> updateLocation(
            @PathVariable UUID id,
            @Valid @RequestBody Location updatedLocation
    ) {
        Optional<Location> optionalLocation = locationRepository.findById(id);

        if (optionalLocation.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Location not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        Location existingLocation = optionalLocation.get();

        if (updatedLocation.getName() != null) existingLocation.setName(updatedLocation.getName());
        if (updatedLocation.getAddress() != null) existingLocation.setAddress(updatedLocation.getAddress());
        if (updatedLocation.getLatitude() != null) existingLocation.setLatitude(updatedLocation.getLatitude());
        if (updatedLocation.getLongitude() != null) existingLocation.setLongitude(updatedLocation.getLongitude());
        if (updatedLocation.getArrivalDate() != null) existingLocation.setArrivalDate(updatedLocation.getArrivalDate());
        if (updatedLocation.getDepartureDate() != null) existingLocation.setDepartureDate(updatedLocation.getDepartureDate());
        if (updatedLocation.getBudget() != null) existingLocation.setBudget(updatedLocation.getBudget());
        if (updatedLocation.getNotes() != null) existingLocation.setNotes(updatedLocation.getNotes());

        if (!existingLocation.isTimeRangeValid()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Validation error: Departure before arrival");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        return ResponseEntity.ok((Object) locationRepository.save(existingLocation));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> deleteLocation(@PathVariable UUID id) {
        if (!locationRepository.existsById(id)) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Location not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
        locationRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}