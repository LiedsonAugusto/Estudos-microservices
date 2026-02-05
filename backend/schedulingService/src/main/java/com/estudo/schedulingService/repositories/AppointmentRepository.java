package com.estudo.schedulingService.repositories;

import com.estudo.schedulingService.entities.Appointment;
import com.estudo.schedulingService.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID>, JpaSpecificationExecutor<Appointment> {
    boolean existsByConfirmationCode(String confirmationCode);
    List<Appointment> findByUserId(UUID userId);
    Optional<Appointment> findByConfirmationCode(String confirmationCode);

    /**
     * verifica se o usuário já tem um agendamento ativo no mesmo horário (mesmo TimeSlot).
     * considera apenas agendamentos que não foram cancelados.
     */
    @Query("SELECT COUNT(a) > 0 FROM Appointment a " +
           "WHERE a.userId = :userId " +
           "AND a.timeSlot.id = :timeSlotId " +
           "AND a.status NOT IN (:excludedStatuses)")
    boolean existsActiveAppointmentForUserInTimeSlot(
            @Param("userId") UUID userId,
            @Param("timeSlotId") UUID timeSlotId,
            @Param("excludedStatuses") List<AppointmentStatus> excludedStatuses
    );

    /**
     * verifica se o usuário já tem um agendamento ativo para o mesmo serviço na mesma data.
     * considera apenas agendamentos que não foram cancelados.
     */
    @Query("SELECT COUNT(a) > 0 FROM Appointment a " +
           "WHERE a.userId = :userId " +
           "AND a.timeSlot.service.id = :serviceId " +
           "AND a.timeSlot.date = :date " +
           "AND a.status NOT IN (:excludedStatuses)")
    boolean existsActiveAppointmentForUserInServiceAndDate(
            @Param("userId") UUID userId,
            @Param("serviceId") UUID serviceId,
            @Param("date") LocalDate date,
            @Param("excludedStatuses") List<AppointmentStatus> excludedStatuses
    );
}
