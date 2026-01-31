package com.estudo.schedulingService.controllers;

import com.estudo.schedulingService.dtos.CreateServiceRequest;
import com.estudo.schedulingService.dtos.PageResponse;
import com.estudo.schedulingService.dtos.ServiceResponse;
import com.estudo.schedulingService.entities.Services;
import com.estudo.schedulingService.services.ServicesService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/services")
public class ServiceController {

    private final ServicesService servicesService;

    public ServiceController(ServicesService servicesService) {
        this.servicesService = servicesService;
    }

    @PostMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceResponse> createService(@Valid @RequestBody CreateServiceRequest
                                                                 serviceDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicesService.create(serviceDTO));
    }

    @GetMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ServiceResponse>> getAllServices() {
        return ResponseEntity.ok(servicesService.getAllServices());
    }

    @GetMapping("/paginated")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<ServiceResponse>> getAllServicesPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction
    ) {
        Pageable pageable = createPageable(page, size, sortBy, direction);
        return ResponseEntity.ok(servicesService.getAllServicesPaginated(pageable));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<ServiceResponse>> searchServices(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Integer durationMinutes,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction
    ) {
        Pageable pageable = createPageable(page, size, sortBy, direction);
        return ResponseEntity.ok(servicesService.searchServices(name, description, durationMinutes,
                active, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceResponse> getServiceById(@PathVariable UUID id) {
        // TODO: implementar
        return null;
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceResponse> putServiceById(@PathVariable UUID id) {
        // TODO: implementar
        return null;
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteServiceById(@PathVariable UUID id) {
        // TODO: implementar
        return null;
    }

    private Pageable createPageable(int page, int size, String sortBy, String direction) {
        if (page < 0) {
            page = 0;
        }
        if (size < 1 || size > 100) {
            size = 10;
        }

        Sort.Direction sortDirection = direction.equalsIgnoreCase("ASC")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
    }
}
