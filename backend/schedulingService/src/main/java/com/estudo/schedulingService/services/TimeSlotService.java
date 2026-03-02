package com.estudo.schedulingService.services;

import com.estudo.schedulingService.dtos.CreateTimeSlotRequest;
import com.estudo.schedulingService.dtos.CreateTimeSlotsInBatchRequest;
import com.estudo.schedulingService.dtos.PageResponse;
import com.estudo.schedulingService.dtos.ServiceResponse;
import com.estudo.schedulingService.dtos.TimeSlotResponse;
import com.estudo.schedulingService.entities.Services;
import com.estudo.schedulingService.entities.TimeSlot;
import com.estudo.schedulingService.exceptions.InactiveServiceException;
import com.estudo.schedulingService.exceptions.InvalidTimeRangeException;
import com.estudo.schedulingService.exceptions.ServiceNotFoundException;
import com.estudo.schedulingService.exceptions.TimeSlotConflictException;
import com.estudo.schedulingService.exceptions.TimeSlotHasBookingsException;
import com.estudo.schedulingService.exceptions.TimeSlotNotFoundException;
import com.estudo.schedulingService.repositories.ServicesRespository;
import com.estudo.schedulingService.repositories.TimeSlotRepository;
import com.estudo.schedulingService.specifications.TimeSlotSpecification;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class TimeSlotService {

    private final TimeSlotRepository timeSlotRepository;
    private final ServicesRespository servicesRepository;
    private final ServicesService servicesService;

    public TimeSlotService(TimeSlotRepository timeSlotRepository,
                           ServicesRespository servicesRepository,
                           ServicesService servicesService) {
        this.timeSlotRepository = timeSlotRepository;
        this.servicesRepository = servicesRepository;
        this.servicesService = servicesService;
    }

    @Transactional
    public TimeSlotResponse create(CreateTimeSlotRequest request) {

        Services service = servicesRepository.findById(request.serviceId())
                .orElseThrow(() -> new ServiceNotFoundException("Serviço não encontrado com ID: " + request.serviceId()));

        if (!service.isActive()) {
            throw new InactiveServiceException("Não é possível criar horário para um serviço inativo");
        }

        if (!request.endTime().isAfter(request.startTime())) {
            throw new InvalidTimeRangeException("O horário de término deve ser após o horário de início");
        }

        boolean hasConflict = timeSlotRepository.existsConflictingTimeSlot(
                request.serviceId(),
                request.date(),
                request.startTime(),
                request.endTime()
        );

        if (hasConflict) {
            throw new TimeSlotConflictException("Já existe um horário cadastrado que conflita com este período");
        }

        // 4. Criar a entidade
        TimeSlot timeSlot = new TimeSlot();
        timeSlot.setService(service);
        timeSlot.setDate(request.date());
        timeSlot.setStartTime(request.startTime());
        timeSlot.setEndTime(request.endTime());
        timeSlot.setCapacity(request.capacity());
        timeSlot.setBookedCount(0);
        timeSlot.setActive(true);

        TimeSlot saved = timeSlotRepository.save(timeSlot);
        return mapToResponse(saved);
    }

    @Transactional
    public List<TimeSlotResponse> createBatch(CreateTimeSlotsInBatchRequest request) {
        Services service = servicesRepository.findById(request.serviceId())
                .orElseThrow(() -> new ServiceNotFoundException("Serviço não encontrado com ID: " + request.serviceId()));

        if (!service.isActive()) {
            throw new InactiveServiceException("Não é possível criar horários para um serviço inativo");
        }

        if (!request.endDate().isAfter(request.startDate()) && !request.endDate().isEqual(request.startDate())) {
            throw new InvalidTimeRangeException("A data final deve ser igual ou posterior à data inicial");
        }

        if (!request.endTime().isAfter(request.startTime())) {
            throw new InvalidTimeRangeException("O horário de término deve ser após o horário de início");
        }

        List<TimeSlot> timeSlotsToSave = new ArrayList<>();
        LocalDate currentDate = request.startDate();

        // itera por cada dia do período
        while (!currentDate.isAfter(request.endDate())) {

            // só cria slots nos dias da semana selecionados
            if (request.daysOfWeek().contains(currentDate.getDayOfWeek())) {
                LocalTime slotStart = request.startTime();

                // gera os slots do dia, dividindo o período pela duração de cada slot
                while (slotStart.plusMinutes(request.slotDurationMinutes()).isBefore(request.endTime())
                        || slotStart.plusMinutes(request.slotDurationMinutes()).equals(request.endTime())) {

                    LocalTime slotEnd = slotStart.plusMinutes(request.slotDurationMinutes());

                    boolean hasConflict = timeSlotRepository.existsConflictingTimeSlot(
                            request.serviceId(),
                            currentDate,
                            slotStart,
                            slotEnd
                    );

                    if (!hasConflict) {
                        TimeSlot timeSlot = new TimeSlot();
                        timeSlot.setService(service);
                        timeSlot.setDate(currentDate);
                        timeSlot.setStartTime(slotStart);
                        timeSlot.setEndTime(slotEnd);
                        timeSlot.setCapacity(request.capacity());
                        timeSlot.setBookedCount(0);
                        timeSlot.setActive(true);
                        timeSlotsToSave.add(timeSlot);
                    }

                    slotStart = slotEnd;
                }
            }

            currentDate = currentDate.plusDays(1);
        }

        if (timeSlotsToSave.isEmpty()) {
            throw new TimeSlotConflictException("Nenhum horário pôde ser criado. Verifique se já existem horários cadastrados no período.");
        }

        List<TimeSlot> saved = timeSlotRepository.saveAll(timeSlotsToSave);
        return saved
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<TimeSlotResponse> getAllTimeSlots() {
        return timeSlotRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public PageResponse<TimeSlotResponse> getAllTimeSlotsPaginated(Pageable pageable) {
        Page<TimeSlot> page = timeSlotRepository.findAll(pageable);

        List<TimeSlotResponse> content = page.getContent()
                .stream()
                .map(this::mapToResponse)
                .toList();

        return new PageResponse<>(
                content,
                page.getNumber(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.getSize(),
                page.hasNext(),
                page.hasPrevious(),
                page.isFirst(),
                page.isLast()
        );
    }

    public PageResponse<TimeSlotResponse> searchTimeSlots(UUID serviceId, LocalDate date,
                                                          Boolean available, Boolean active,
                                                          Pageable pageable) {
        Specification<TimeSlot> spec = TimeSlotSpecification.withFilters(
                serviceId, date, available, active
        );

        Page<TimeSlot> page = timeSlotRepository.findAll(spec, pageable);

        List<TimeSlotResponse> content = page.getContent()
                .stream()
                .map(this::mapToResponse)
                .toList();

        return new PageResponse<>(
                content,
                page.getNumber(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.getSize(),
                page.hasNext(),
                page.hasPrevious(),
                page.isFirst(),
                page.isLast()
        );
    }

    public TimeSlotResponse getTimeSlotById(UUID id) {
        TimeSlot timeSlot = timeSlotRepository.findById(id)
                .orElseThrow(() -> new TimeSlotNotFoundException("Horário não encontrado com ID: " + id));

        return mapToResponse(timeSlot);
    }

    @Transactional
    public TimeSlotResponse updateTimeSlot(UUID id, CreateTimeSlotRequest request) {
        TimeSlot timeSlot = timeSlotRepository.findById(id)
                .orElseThrow(() -> new TimeSlotNotFoundException("Horário não encontrado com ID: " + id));

        // não permite alterar horário que já tem agendamentos
        if (timeSlot.getBookedCount() > 0) {
            throw new TimeSlotHasBookingsException("Não é possível alterar um horário que já possui agendamentos");
        }

        // valida se o novo serviço existe e está ativo
        Services service = servicesRepository.findById(request.serviceId())
                .orElseThrow(() -> new ServiceNotFoundException("Serviço não encontrado com ID: " + request.serviceId()));

        if (!service.isActive()) {
            throw new InactiveServiceException("Não é possível vincular horário a um serviço inativo");
        }

        if (!request.endTime().isAfter(request.startTime())) {
            throw new InvalidTimeRangeException("O horário de término deve ser após o horário de início");
        }

        // verifica conflito apenas se mudou data/horário/serviço
        boolean changedSchedule = !timeSlot.getDate().equals(request.date())
                || !timeSlot.getStartTime().equals(request.startTime())
                || !timeSlot.getEndTime().equals(request.endTime())
                || !timeSlot.getService().getId().equals(request.serviceId());

        if (changedSchedule) {
            boolean hasConflict = timeSlotRepository.existsConflictingTimeSlotExcludingId(
                    request.serviceId(),
                    request.date(),
                    request.startTime(),
                    request.endTime(),
                    id
            );

            if (hasConflict) {
                throw new TimeSlotConflictException("Já existe um horário cadastrado que conflita com este período");
            }
        }

        timeSlot.setService(service);
        timeSlot.setDate(request.date());
        timeSlot.setStartTime(request.startTime());
        timeSlot.setEndTime(request.endTime());
        timeSlot.setCapacity(request.capacity());

        TimeSlot updated = timeSlotRepository.save(timeSlot);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteTimeSlot(UUID id) {
        TimeSlot timeSlot = timeSlotRepository.findById(id)
                .orElseThrow(() -> new TimeSlotNotFoundException("Horário não encontrado com ID: " + id));

        // soft delete - apenas desativa
        timeSlot.setActive(false);
        timeSlotRepository.save(timeSlot);
    }

    public TimeSlotResponse mapToResponse(TimeSlot timeSlot) {
        ServiceResponse serviceResponse = servicesService.mapToServiceResponse(timeSlot.getService());

        return new TimeSlotResponse(
                timeSlot.getId(),
                serviceResponse,
                timeSlot.getDate(),
                timeSlot.getStartTime(),
                timeSlot.getEndTime(),
                timeSlot.getCapacity(),
                timeSlot.getCapacity() - timeSlot.getBookedCount(), // availableSpots
                timeSlot.isActive()
        );
    }
}
