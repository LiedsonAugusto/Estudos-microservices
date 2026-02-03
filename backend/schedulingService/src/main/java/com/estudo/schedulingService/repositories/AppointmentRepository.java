package com.estudo.schedulingService.repositories;

import com.estudo.schedulingService.entities.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID>, JpaSpecificationExecutor<Appointment> {
    boolean existsByConfirmationCode(String confirmationCode);
    List<Appointment> findByUserId(UUID userId);
    Optional<Appointment> findByConfirmationCode(String confirmationCode);
}
