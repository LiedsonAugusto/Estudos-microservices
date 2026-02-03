package com.estudo.schedulingService.entities;

import com.estudo.schedulingService.enums.AppointmentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "appointments")
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;  // referência ao user-service

    @Column(nullable = false)
    private String userEmail;  // desnormalizado para o email-service

    @Column(nullable = false)
    private String userName;  // desnormalizado para o email-service

    @ManyToOne
    @JoinColumn(name = "time_slot_id", nullable = false)
    private TimeSlot timeSlot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status;

    private String notes;  // observações do cidadão

    private String cancellationReason;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Column(unique = true, nullable = false)
    private String confirmationCode;  // código de 8 caracteres para check-in
}
