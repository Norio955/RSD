package com.sumdu.travelerapi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "locations")
@Data
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travel_plan_id", nullable = false)
    @JsonIgnore
    private TravelPlan travelPlan;

    @JsonProperty("travel_plan_id")
    public UUID getTravelPlanId() {
        return travelPlan != null ? travelPlan.getId() : null;
    }

    @NotBlank(message = "Name cannot be blank")
    @Size(max = 200, message = "Name must not exceed 200 characters")
    @Column(nullable = false, length = 200)
    private String name;

    private String address;

    @DecimalMin(value = "-90.0", message = "Latitude must be >= -90")
    @DecimalMax(value = "90.0", message = "Latitude must be <= 90")
    private BigDecimal latitude;

    @DecimalMin(value = "-180.0", message = "Longitude must be >= -180")
    @DecimalMax(value = "180.0", message = "Longitude must be <= 180")
    private BigDecimal longitude;

    private OffsetDateTime arrivalDate;
    private OffsetDateTime departureDate;

    private Integer visitOrder;

    @DecimalMin(value = "0.0", message = "Budget cannot be negative")
    private BigDecimal budget;

    private String notes;

    @AssertTrue(message = "Departure date cannot be before arrival date")
    @JsonIgnore
    public boolean isTimeRangeValid() {
        if (arrivalDate == null || departureDate == null) {
            return true;
        }
        return !departureDate.isBefore(arrivalDate);
    }
}