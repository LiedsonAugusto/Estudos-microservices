package com.estudo.userService.specifications;

import com.estudo.userService.entities.User;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {

    public static Specification<User> hasName(String name) {
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

    public static Specification<User> hasEmail(String email) {
        return (root, query, criteriaBuilder) -> {
            if (email == null || email.isBlank()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("email")),
                    "%" + email.toLowerCase() + "%"
            );
        };
    }

    public static Specification<User> hasCpf(String cpf) {
        return (root, query, criteriaBuilder) -> {
            if (cpf == null || cpf.isBlank()) {
                return criteriaBuilder.conjunction();
            }
            // Remove caracteres especiais do CPF para busca flexível
            String cleanCpf = cpf.replaceAll("[^0-9]", "");
            return criteriaBuilder.like(
                    root.get("cpf"),
                    "%" + cleanCpf + "%"
            );
        };
    }

    public static Specification<User> withFilters(String name, String email, String cpf) {
        return Specification.where(hasName(name))
                .and(hasEmail(email))
                .and(hasCpf(cpf));
    }
}
