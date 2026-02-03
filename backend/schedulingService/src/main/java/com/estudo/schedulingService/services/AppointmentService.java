package com.estudo.schedulingService.services;

import com.estudo.schedulingService.dtos.AppointmentResponse;
import com.estudo.schedulingService.dtos.CreateAppointmentRequest;
import com.estudo.schedulingService.dtos.TimeSlotResponse;
import com.estudo.schedulingService.entities.Appointment;
import com.estudo.schedulingService.entities.TimeSlot;
import com.estudo.schedulingService.enums.AppointmentStatus;
import com.estudo.schedulingService.exceptions.AppointmentNotFoundException;
import com.estudo.schedulingService.exceptions.NoSlotsAvailableException;
import com.estudo.schedulingService.exceptions.TimeSlotNotFoundException;
import com.estudo.schedulingService.repositories.AppointmentRepository;
import com.estudo.schedulingService.repositories.TimeSlotRepository;
import com.estudo.schedulingService.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final TimeSlotService timeSlotService;
    private final JwtService jwtService;
    private final HttpServletRequest request;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              TimeSlotRepository timeSlotRepository,
                              TimeSlotService timeSlotService,
                              JwtService jwtService,
                              HttpServletRequest request) {
        this.appointmentRepository = appointmentRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.timeSlotService = timeSlotService;
        this.jwtService = jwtService;
        this.request = request;
    }

    @Transactional
    public AppointmentResponse create(CreateAppointmentRequest createRequest) {

        String token = extractTokenFromRequest();
        UUID userId = jwtService.extractUserId(token);
        String userEmail = jwtService.extractUsername(token);
        String userName = jwtService.extractName(token);

        TimeSlot timeSlot = timeSlotRepository.findById(createRequest.timeSlotId())
                .orElseThrow(() -> new TimeSlotNotFoundException("Horário não encontrado"));

        if (!timeSlot.isActive()) {
            throw new IllegalArgumentException("Este horário não está mais disponível");
        }

        if (timeSlot.getBookedCount() >= timeSlot.getCapacity()) {
            throw new NoSlotsAvailableException("Não há mais vagas disponíveis para este horário");
        }

        Appointment appointment = new Appointment();
        appointment.setUserId(userId);
        appointment.setUserEmail(userEmail);
        appointment.setUserName(userName);
        appointment.setTimeSlot(timeSlot);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setNotes(createRequest.notes());
        appointment.setConfirmationCode(generateConfirmationCode());
        appointment.setCreatedAt(LocalDateTime.now());

        Appointment savedAppointment = appointmentRepository.save(appointment);

        timeSlot.setBookedCount(timeSlot.getBookedCount() + 1);
        timeSlotRepository.save(timeSlot);

        return mapToAppointmentResponse(savedAppointment);
    }

    public List<AppointmentResponse> findAll() {
        String token = extractTokenFromRequest();
        UUID userId = jwtService.extractUserId(token);
        String role = jwtService.extractRole(token);

        List<Appointment> appointments;
        if ("ADMIN".equals(role)) {
            appointments = appointmentRepository.findAll();
        } else {
            appointments = appointmentRepository.findByUserId(userId);
        }

        return appointments.stream()
                .map(this::mapToAppointmentResponse)
                .collect(Collectors.toList());
    }

    public AppointmentResponse findById(UUID id) {
        String token = extractTokenFromRequest();
        UUID userId = jwtService.extractUserId(token);
        String role = jwtService.extractRole(token);

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException("Agendamento não encontrado"));

        // Verificar se o usuário tem permissão para ver este appointment
        if (!"ADMIN".equals(role) && !appointment.getUserId().equals(userId)) {
            throw new AppointmentNotFoundException("Agendamento não encontrado");
        }

        return mapToAppointmentResponse(appointment);
    }

    public AppointmentResponse findByConfirmationCode(String confirmationCode) {
        String token = extractTokenFromRequest();
        UUID userId = jwtService.extractUserId(token);
        String role = jwtService.extractRole(token);

        Appointment appointment = appointmentRepository.findByConfirmationCode(confirmationCode)
                .orElseThrow(() -> new AppointmentNotFoundException("Agendamento não encontrado com este código de confirmação"));

        // Verificar se o usuário tem permissão para ver este appointment
        if (!"ADMIN".equals(role) && !appointment.getUserId().equals(userId)) {
            throw new AppointmentNotFoundException("Agendamento não encontrado com este código de confirmação");
        }

        return mapToAppointmentResponse(appointment);
    }

    private String extractTokenFromRequest() {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new IllegalArgumentException("Token JWT não encontrado");
    }

    private String generateConfirmationCode() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder code = new StringBuilder(8);

        for (int i = 0; i < 8; i++) {
            code.append(characters.charAt(random.nextInt(characters.length())));
        }

        // Verificar se o código já existe (improvável, mas por segurança)
        while (appointmentRepository.existsByConfirmationCode(code.toString())) {
            code = new StringBuilder(8);
            for (int i = 0; i < 8; i++) {
                code.append(characters.charAt(random.nextInt(characters.length())));
            }
        }

        return code.toString();
    }

    private AppointmentResponse mapToAppointmentResponse(Appointment appointment) {
        TimeSlotResponse timeSlotResponse = timeSlotService.mapToResponse(appointment.getTimeSlot());

        return new AppointmentResponse(
                appointment.getId(),
                appointment.getUserId(),
                appointment.getUserName(),
                timeSlotResponse,
                appointment.getStatus(),
                appointment.getNotes(),
                appointment.getConfirmationCode(),
                appointment.getCreatedAt()
        );
    }
}
