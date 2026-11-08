package com.sumdu.travelerapi.controller;

import com.sumdu.travelerapi.model.Location;
import com.sumdu.travelerapi.model.TravelPlan;
import com.sumdu.travelerapi.repository.LocationRepository;
import com.sumdu.travelerapi.repository.TravelPlanRepository;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/travel-plans")
public class TravelPlanController {

    private final TravelPlanRepository travelPlanRepository;
    private final LocationRepository locationRepository;

    public TravelPlanController(
            TravelPlanRepository travelPlanRepository,
            LocationRepository locationRepository
    ) {
        this.travelPlanRepository = travelPlanRepository;
        this.locationRepository = locationRepository;
    }


    @GetMapping
    public ResponseEntity<List<TravelPlan>> getAllPlans() {
        List<TravelPlan> plans = travelPlanRepository.findAll();
        return ResponseEntity.ok(plans);
    }

    @PostMapping
    public ResponseEntity<?> createPlan(
            @Valid @RequestBody TravelPlan plan
    ) {
        plan.setVersion(1);

        TravelPlan savedPlan = travelPlanRepository.save(plan);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedPlan);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPlanById(
            @PathVariable UUID id
    ) {
        Optional<TravelPlan> optionalPlan =
                travelPlanRepository.findById(id);

        if (optionalPlan.isEmpty()) {
            return errorResponse(
                    HttpStatus.NOT_FOUND,
                    "Travel plan not found"
            );
        }

        return ResponseEntity.ok(optionalPlan.get());
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> updatePlan(
            @PathVariable UUID id,
            @Valid @RequestBody TravelPlan updatedPlan
    ) {
        // Проверяем наличие версии
        if (updatedPlan.getVersion() == null ||
                updatedPlan.getVersion() < 1) {

            return errorResponse(
                    HttpStatus.BAD_REQUEST,
                    "Validation error: Version is required and must be positive"
            );
        }

        Optional<TravelPlan> optionalPlan =
                travelPlanRepository.findById(id);

        if (optionalPlan.isEmpty()) {
            return errorResponse(
                    HttpStatus.NOT_FOUND,
                    "Travel plan not found"
            );
        }

        TravelPlan existingPlan = optionalPlan.get();

        // Проверка optimistic locking
        if (!updatedPlan.getVersion().equals(existingPlan.getVersion())) {
            throw new ObjectOptimisticLockingFailureException(
                    TravelPlan.class,
                    id
            );
        }

        // Обновление полей
        existingPlan.setTitle(updatedPlan.getTitle());

        if (updatedPlan.getDescription() != null) {
            existingPlan.setDescription(updatedPlan.getDescription());
        }

        if (updatedPlan.getStartDate() != null) {
            existingPlan.setStartDate(updatedPlan.getStartDate());
        }

        if (updatedPlan.getEndDate() != null) {
            existingPlan.setEndDate(updatedPlan.getEndDate());
        }

        if (updatedPlan.getBudget() != null) {
            existingPlan.setBudget(updatedPlan.getBudget());
        }

        if (updatedPlan.getCurrency() != null) {
            existingPlan.setCurrency(updatedPlan.getCurrency());
        }

        if (updatedPlan.getIsPublic() != null) {
            existingPlan.setIsPublic(updatedPlan.getIsPublic());
        }

        // Проверка дат
        if (!existingPlan.isDateRangeValid()) {
            return errorResponse(
                    HttpStatus.BAD_REQUEST,
                    "Validation error: End date cannot be before start date"
            );
        }

        // Сохраняем изменения.
        // Hibernate самостоятельно увеличивает @Version.
        TravelPlan savedPlan =
                travelPlanRepository.save(existingPlan);

        return ResponseEntity.ok(savedPlan);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> deletePlan(
            @PathVariable UUID id
    ) {
        if (!travelPlanRepository.existsById(id)) {
            return errorResponse(
                    HttpStatus.NOT_FOUND,
                    "Travel plan not found"
            );
        }

        travelPlanRepository.deleteById(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/locations")
    @Transactional
    public ResponseEntity<?> addLocationToPlan(
            @PathVariable UUID id,
            @Valid @RequestBody Location location
    ) {
        Optional<TravelPlan> optionalPlan =
                travelPlanRepository.findById(id);

        if (optionalPlan.isEmpty()) {
            return errorResponse(
                    HttpStatus.NOT_FOUND,
                    "Travel plan not found"
            );
        }

        TravelPlan plan = optionalPlan.get();

        // Привязываем локацию к поездке
        location.setTravelPlan(plan);

        // Определяем следующий порядковый номер
        int nextOrder = (int)
                locationRepository.countByTravelPlan_Id(id) + 1;

        location.setVisitOrder(nextOrder);

        // Сохраняем локацию
        Location savedLocation =
                locationRepository.save(location);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedLocation);
    }

    private ResponseEntity<Map<String, String>> errorResponse(
            HttpStatus status,
            String message
    ) {
        Map<String, String> error = new HashMap<>();

        error.put("error", message);

        return ResponseEntity
                .status(status)
                .body(error);
    }
}
