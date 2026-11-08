package com.sumdu.travelerapi.repository;

import com.sumdu.travelerapi.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LocationRepository extends JpaRepository<Location, UUID> {

    List<Location> findByTravelPlan_IdOrderByVisitOrderAsc(UUID travelPlanId);

    long countByTravelPlan_Id(UUID travelPlanId);
}