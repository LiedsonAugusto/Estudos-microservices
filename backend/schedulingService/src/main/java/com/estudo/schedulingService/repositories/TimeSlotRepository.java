package com.estudo.schedulingService.repositories;

import com.estudo.schedulingService.entities.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, UUID>, JpaSpecificationExecutor<TimeSlot> {

    @Query("""
            SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END
            FROM TimeSlot t
            WHERE t.service.id = :serviceId
            AND t.date = :date
            AND t.active = true
            AND (
                (t.startTime < :endTime AND t.endTime > :startTime)
            )
            """)
    boolean existsConflictingTimeSlot(
            @Param("serviceId") UUID serviceId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );

    @Query("""
            SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END
            FROM TimeSlot t
            WHERE t.service.id = :serviceId
            AND t.date = :date
            AND t.active = true
            AND t.id != :excludeId
            AND (
                (t.startTime < :endTime AND t.endTime > :startTime)
            )
            """)
    boolean existsConflictingTimeSlotExcludingId(
            @Param("serviceId") UUID serviceId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeId") UUID excludeId
    );
}
