package com.estudo.schedulingService.specifications;

import com.estudo.schedulingService.entities.Services;
import org.springframework.data.jpa.domain.Specification;

public class ServicesSpecification {

    public static Specification<Services> hasName(String name) {
        return (root, query, criteriaBuilder) -> {
            if (name == null || name.isBlank()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")),
                    "%" + name.toLowerCase() + "%"
            );
        };
    }

    public static Specification<Services> hasDescription(String description) {
        return (root, query, criteriaBuilder) -> {
            if (description == null || description.isBlank()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("description")),  // ✅ description
                    "%" + description.toLowerCase() + "%"
            );
        };
    }

    public static Specification<Services> hasDurationMinutes(Integer durationMinutes) {
        return (root, query, criteriaBuilder) -> {
            if (durationMinutes == null) {  // ✅ só verifica null
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(  // ✅ equal para números
                    root.get("durationMinutes"),
                    durationMinutes
            );
        };
    }

    public static Specification<Services> isActive(Boolean active) {
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

    public static Specification<Services> withFilters(String name, String description,
                                                      Integer durationMinutes, Boolean active) {
        return Specification.where(hasName(name))
                .and(hasDescription(description))
                .and(hasDurationMinutes(durationMinutes))
                .and(isActive(active));
    }
}
