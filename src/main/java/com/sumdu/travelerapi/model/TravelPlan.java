package com.sumdu.travelerapi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "travel_plans")
@Getter
@Setter
public class TravelPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Название обязательно, максимум 200 символов
    @NotBlank(message = "Title cannot be blank")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    @Column(nullable = false, length = 200)
    private String title;

    private String description;

    private LocalDate startDate;

    private LocalDate endDate;

    // Бюджет не может быть отрицательным
    // Максимум 8 цифр до точки и 2 после
    @DecimalMin(value = "0.0", message = "Budget cannot be negative")
    @Digits(
            integer = 8,
            fraction = 2,
            message = "Budget must have at most 2 decimal places"
    )
    @Column(precision = 10, scale = 2)
    private BigDecimal budget;

    // Валюта должна содержать ровно 3 заглавные буквы
    @Pattern(
            regexp = "^[A-Z]{3}$",
            message = "Currency must contain exactly 3 uppercase letters"
    )
    @Column(length = 3)
    private String currency;

    private Boolean isPublic;

    // Hibernate самостоятельно управляет версией
    @Version
    @Min(value = 1, message = "Version must be positive")
    private Integer version;

    // Связанные локации
    @OneToMany(
            mappedBy = "travelPlan",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Location> locations = new ArrayList<>();

    // Проверка диапазона дат
    @AssertTrue(message = "End date cannot be before start date")
    @JsonIgnore
    public boolean isDateRangeValid() {
        if (startDate == null || endDate == null) {
            return true;
        }

        return !endDate.isBefore(startDate);
    }

    // Установка начальных значений перед сохранением
    @PrePersist
    public void prePersist() {

        if (this.version == null) {
            this.version = 1;
        }

        if (this.currency == null) {
            this.currency = "USD";
        }

        if (this.isPublic == null) {
            this.isPublic = false;
        }
    }
}