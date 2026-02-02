package com.estudo.schedulingService.specifications;

import com.estudo.schedulingService.entities.Appointment;
import com.estudo.schedulingService.enums.AppointmentStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.UUID;

public class AppointmentSpecification {

    public static Specification<Appointment> hasUserId(UUID userId) {
        return (root, query, criteriaBuilder) -> {
            if (userId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(
                    root.get("userId"),
                    userId
            );
        };
    }

    public static Specification<Appointment> hasStatus(AppointmentStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(
                    root.get("status"),
                    status
            );
        };
    }

    public static Specification<Appointment> hasConfirmationCode(String confirmationCode) {
        return (root, query, criteriaBuilder) -> {
            if (confirmationCode == null || confirmationCode.isBlank()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(
                    root.get("confirmationCode"),
                    confirmationCode
            );
        };
    }

    public static Specification<Appointment> hasTimeSlotId(UUID timeSlotId) {
        return (root, query, criteriaBuilder) -> {
            if (timeSlotId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(
                    root.get("timeSlot").get("id"),
                    timeSlotId
            );
        };
    }

    public static Specification<Appointment> hasServiceId(UUID serviceId) {
        return (root, query, criteriaBuilder) -> {
            if (serviceId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(
                    root.get("timeSlot").get("service").get("id"),
                    serviceId
            );
        };
    }

    public static Specification<Appointment> hasDate(LocalDate date) {
        return (root, query, criteriaBuilder) -> {
            if (date == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(
                    root.get("timeSlot").get("date"),
                    date
            );
        };
    }

    public static Specification<Appointment> hasDateRange(LocalDate startDate, LocalDate endDate) {
        return (root, query, criteriaBuilder) -> {
            if (startDate == null && endDate == null) {
                return criteriaBuilder.conjunction();
            }
            if (startDate != null && endDate != null) {
                return criteriaBuilder.between(
                        root.get("timeSlot").get("date"),
                        startDate,
                        endDate
                );
            }
            if (startDate != null) {
                return criteriaBuilder.greaterThanOrEqualTo(
                        root.get("timeSlot").get("date"),
                        startDate
                );
            }
            return criteriaBuilder.lessThanOrEqualTo(
                    root.get("timeSlot").get("date"),
                    endDate
            );
        };
    }

    public static Specification<Appointment> withFilters(UUID userId, AppointmentStatus status,
                                                         UUID serviceId, LocalDate date) {
        return Specification.where(hasUserId(userId))
                .and(hasStatus(status))
                .and(hasServiceId(serviceId))
                .and(hasDate(date));
    }

    public static Specification<Appointment> withFiltersAndDateRange(UUID userId, AppointmentStatus status,
                                                                      UUID serviceId, LocalDate startDate,
                                                                      LocalDate endDate) {
        return Specification.where(hasUserId(userId))
                .and(hasStatus(status))
                .and(hasServiceId(serviceId))
                .and(hasDateRange(startDate, endDate));
    }
}
