package com.estudo.schedulingService.specifications;

import com.estudo.schedulingService.entities.TimeSlot;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.UUID;

public class TimeSlotSpecification {

    public static Specification<TimeSlot> hasServiceId(UUID serviceId) {
        return (root, query, criteriaBuilder) -> {
            if (serviceId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(
                    root.get("service").get("id"),
                    serviceId
            );
        };
    }

    public static Specification<TimeSlot> hasDate(LocalDate date) {
        return (root, query, criteriaBuilder) -> {
            if (date == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(
                    root.get("date"),
                    date
            );
        };
    }

    public static Specification<TimeSlot> hasDateRange(LocalDate startDate, LocalDate endDate) {
        return (root, query, criteriaBuilder) -> {
            if (startDate == null && endDate == null) {
                return criteriaBuilder.conjunction();
            }
            if (startDate != null && endDate != null) {
                return criteriaBuilder.between(
                        root.get("date"),
                        startDate,
                        endDate
                );
            }
            if (startDate != null) {
                return criteriaBuilder.greaterThanOrEqualTo(
                        root.get("date"),
                        startDate
                );
            }
            return criteriaBuilder.lessThanOrEqualTo(
                    root.get("date"),
                    endDate
            );
        };
    }

    public static Specification<TimeSlot> isAvailable(Boolean available) {
        return (root, query, criteriaBuilder) -> {
            if (available == null || !available) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThan(
                    root.get("bookedCount"),
                    root.get("capacity")
            );
        };
    }

    public static Specification<TimeSlot> isActive(Boolean active) {
        return (root, query, criteriaBuilder) -> {
            if (active == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(
                    root.get("active"),
                    active
            );
        };
    }

    public static Specification<TimeSlot> withFilters(UUID serviceId, LocalDate date,
                                                      Boolean available, Boolean active) {
        return Specification.where(hasServiceId(serviceId))
                .and(hasDate(date))
                .and(isAvailable(available))
                .and(isActive(active));
    }

    public static Specification<TimeSlot> withFiltersAndDateRange(UUID serviceId, LocalDate startDate,
                                                                   LocalDate endDate, Boolean available,
                                                                   Boolean active) {
        return Specification.where(hasServiceId(serviceId))
                .and(hasDateRange(startDate, endDate))
                .and(isAvailable(available))
                .and(isActive(active));
    }
}
