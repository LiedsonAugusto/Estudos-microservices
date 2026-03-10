package com.estudo.schedulingService.services;

import com.estudo.schedulingService.dtos.AppointmentResponse;
import com.estudo.schedulingService.dtos.CancelAppointmentRequest;
import com.estudo.schedulingService.dtos.CreateAppointmentRequest;
import com.estudo.schedulingService.dtos.TimeSlotResponse;
import com.estudo.schedulingService.entities.Appointment;
import com.estudo.schedulingService.entities.TimeSlot;
import com.estudo.schedulingService.enums.AppointmentStatus;
import com.estudo.schedulingService.exceptions.AppointmentNotFoundException;
import com.estudo.schedulingService.exceptions.CancellationWindowExpiredException;
import com.estudo.schedulingService.exceptions.DuplicateBookingException;
import com.estudo.schedulingService.exceptions.InvalidAppointmentStatusException;
import com.estudo.schedulingService.exceptions.NoSlotsAvailableException;
import com.estudo.schedulingService.exceptions.TimeSlotNotFoundException;
import com.estudo.schedulingService.producers.AppointmentProducer;
import com.estudo.schedulingService.repositories.AppointmentRepository;
import com.estudo.schedulingService.repositories.TimeSlotRepository;
import com.estudo.schedulingService.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
    private final AppointmentProducer  appointmentProducer;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              TimeSlotRepository timeSlotRepository,
                              TimeSlotService timeSlotService,
                              JwtService jwtService,
                              HttpServletRequest request,
                              AppointmentProducer appointmentProducer) {
        this.appointmentRepository = appointmentRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.timeSlotService = timeSlotService;
        this.jwtService = jwtService;
        this.request = request;
        this.appointmentProducer = appointmentProducer;
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

        // validar se o usuário já tem agendamento no mesmo horário
        List<AppointmentStatus> excludedStatuses = List.of(
                AppointmentStatus.CANCELLED,
                AppointmentStatus.NO_SHOW
        );

        boolean hasAppointmentInSameTimeSlot = appointmentRepository.existsActiveAppointmentForUserInTimeSlot(
                userId,
                timeSlot.getId(),
                excludedStatuses
        );

        if (hasAppointmentInSameTimeSlot) {
            throw new DuplicateBookingException("Você já possui um agendamento neste horário");
        }

        // validar se o usuário já tem agendamento para o mesmo serviço no mesmo dia
        boolean hasAppointmentForServiceInDate = appointmentRepository.existsActiveAppointmentForUserInServiceAndDate(
                userId,
                timeSlot.getService().getId(),
                timeSlot.getDate(),
                excludedStatuses
        );

        if (hasAppointmentForServiceInDate) {
            throw new DuplicateBookingException(
                    "Você já possui um agendamento para este serviço na data " + timeSlot.getDate()
            );
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

        appointmentProducer.publishAppointmentCreated(savedAppointment);

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

        // verificar se o usuário tem permissão para ver este appointment
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

    @Transactional
    public AppointmentResponse cancelAppointment(UUID id, CancelAppointmentRequest cancelRequest) {
        String token = extractTokenFromRequest();
        UUID userId = jwtService.extractUserId(token);
        String role = jwtService.extractRole(token);

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException("Agendamento não encontrado"));

        if (!"ADMIN".equals(role) && !appointment.getUserId().equals(userId)) {
            throw new AppointmentNotFoundException("Agendamento não encontrado");
        }

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new InvalidAppointmentStatusException("Este agendamento já foi cancelado");
        }

        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new InvalidAppointmentStatusException("Não é possível cancelar um agendamento já concluído");
        }

        if (appointment.getStatus() == AppointmentStatus.NO_SHOW) {
            throw new InvalidAppointmentStatusException("Não é possível cancelar um agendamento marcado como não comparecido");
        }

        TimeSlot timeSlot = appointment.getTimeSlot();
        LocalDateTime appointmentDateTime = LocalDateTime.of(timeSlot.getDate(), timeSlot.getStartTime());
        LocalDateTime now = LocalDateTime.now();
        long hoursUntilAppointment = ChronoUnit.HOURS.between(now, appointmentDateTime);

        if (hoursUntilAppointment < 24) {
            throw new CancellationWindowExpiredException("Cancelamento só é permitido até 24 horas antes do horário agendado");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancellationReason(cancelRequest.reason());
        appointment.setUpdatedAt(LocalDateTime.now());

        Appointment updatedAppointment = appointmentRepository.save(appointment);

        timeSlot.setBookedCount(timeSlot.getBookedCount() - 1);
        timeSlotRepository.save(timeSlot);

        appointmentProducer.publishAppointmentCancelled(updatedAppointment, cancelRequest.reason());

        return mapToAppointmentResponse(updatedAppointment);
    }

    @Transactional
    public AppointmentResponse confirmAppointment(UUID id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException("Agendamento não encontrado"));

        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new InvalidAppointmentStatusException(appointment.getStatus(), AppointmentStatus.SCHEDULED);
        }

        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setUpdatedAt(LocalDateTime.now());

        Appointment updatedAppointment = appointmentRepository.save(appointment);

        return mapToAppointmentResponse(updatedAppointment);
    }

    @Transactional
    public AppointmentResponse completeAppointment(UUID id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException("Agendamento não encontrado"));

        if (appointment.getStatus() != AppointmentStatus.CONFIRMED && appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new InvalidAppointmentStatusException(appointment.getStatus(), AppointmentStatus.CONFIRMED, AppointmentStatus.SCHEDULED);
        }

        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointment.setUpdatedAt(LocalDateTime.now());

        Appointment updatedAppointment = appointmentRepository.save(appointment);

        return mapToAppointmentResponse(updatedAppointment);
    }

    @Transactional
    public AppointmentResponse markAsNoShow(UUID id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException("Agendamento não encontrado"));

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new InvalidAppointmentStatusException("Não é possível marcar como não comparecido um agendamento cancelado");
        }

        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new InvalidAppointmentStatusException("Não é possível marcar como não comparecido um agendamento já concluído");
        }

        if (appointment.getStatus() == AppointmentStatus.NO_SHOW) {
            throw new InvalidAppointmentStatusException("Este agendamento já está marcado como não comparecido");
        }

        appointment.setStatus(AppointmentStatus.NO_SHOW);
        appointment.setUpdatedAt(LocalDateTime.now());

        Appointment updatedAppointment = appointmentRepository.save(appointment);

        TimeSlot timeSlot = appointment.getTimeSlot();
        timeSlot.setBookedCount(timeSlot.getBookedCount() - 1);
        timeSlotRepository.save(timeSlot);

        return mapToAppointmentResponse(updatedAppointment);
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
