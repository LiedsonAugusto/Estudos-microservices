package com.estudo.schedulingService.controllers;

import com.estudo.schedulingService.dtos.CreateTimeSlotRequest;
import com.estudo.schedulingService.dtos.CreateTimeSlotsInBatchRequest;
import com.estudo.schedulingService.dtos.PageResponse;
import com.estudo.schedulingService.dtos.TimeSlotResponse;
import com.estudo.schedulingService.services.TimeSlotService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/time-slots")
public class TimeSlotController {

    private final TimeSlotService timeSlotService;

    public TimeSlotController(TimeSlotService timeSlotService) {
        this.timeSlotService = timeSlotService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TimeSlotResponse> create(@Valid @RequestBody CreateTimeSlotRequest request) {
        TimeSlotResponse response = timeSlotService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TimeSlotResponse>> createBatch(@Valid @RequestBody CreateTimeSlotsInBatchRequest request) {
        List<TimeSlotResponse> response = timeSlotService.createBatch(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<TimeSlotResponse>> getAllTimeSlots() {
        List<TimeSlotResponse> response = timeSlotService.getAllTimeSlots();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/paginated")
    public ResponseEntity<PageResponse<TimeSlotResponse>> getAllTimeSlotsPaginated(
            @PageableDefault(size = 20, sort = "date", direction = Sort.Direction.ASC) Pageable pageable) {
        PageResponse<TimeSlotResponse> response = timeSlotService.getAllTimeSlotsPaginated(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<PageResponse<TimeSlotResponse>> searchTimeSlots(
            @RequestParam(required = false) UUID serviceId,
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) Boolean available,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 20, sort = "date", direction = Sort.Direction.ASC) Pageable pageable) {
        PageResponse<TimeSlotResponse> response = timeSlotService.searchTimeSlots(
                serviceId, date, available, active, pageable
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TimeSlotResponse> getTimeSlotById(@PathVariable UUID id) {
        TimeSlotResponse response = timeSlotService.getTimeSlotById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TimeSlotResponse> updateTimeSlot(
            @PathVariable UUID id,
            @Valid @RequestBody CreateTimeSlotRequest request) {
        TimeSlotResponse response = timeSlotService.updateTimeSlot(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTimeSlot(@PathVariable UUID id) {
        timeSlotService.deleteTimeSlot(id);
        return ResponseEntity.noContent().build();
    }
}
