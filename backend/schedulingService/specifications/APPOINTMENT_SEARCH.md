# Appointment - Search e Paginação

## Especificações Disponíveis

O `AppointmentSpecification` oferece os seguintes filtros:

### Filtros Básicos

- **userId**: Filtrar por usuário específico (UUID)
- **status**: Filtrar por status do agendamento (SCHEDULED, CONFIRMED, COMPLETED, CANCELLED, NO_SHOW)
- **confirmationCode**: Buscar por código de confirmação específico
- **timeSlotId**: Filtrar por horário específico (UUID)
- **serviceId**: Filtrar por serviço específico (UUID) - através do timeSlot
- **date**: Filtrar por data específica (LocalDate) - através do timeSlot

### Filtros Avançados

- **dateRange**: Filtrar por intervalo de datas (startDate e endDate)

## Exemplo de Uso no Controller

```java
@GetMapping("/search")
@PreAuthorize("hasAnyRole('ADMIN', 'CITIZEN')")
public ResponseEntity<PageResponse<AppointmentResponse>> searchAppointments(
        @RequestParam(required = false) UUID userId,
        @RequestParam(required = false) AppointmentStatus status,
        @RequestParam(required = false) UUID serviceId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @RequestParam(defaultValue = "DESC") String direction
) {
    Pageable pageable = createPageable(page, size, sortBy, direction);

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
        UUID authenticatedUserId = getUserIdFromAuthentication(auth);
        userId = authenticatedUserId;
    }

    return ResponseEntity.ok(appointmentService.searchAppointments(
            userId, status, serviceId, date, pageable));
}
```

## Exemplo de Uso no Service

```java
public PageResponse<AppointmentResponse> searchAppointments(UUID userId, AppointmentStatus status,
                                                            UUID serviceId, LocalDate date,
                                                            Pageable pageable) {
    Specification<Appointment> specification = AppointmentSpecification.withFilters(
            userId, status, serviceId, date
    );
    Page<Appointment> appointmentsPage = appointmentRepository.findAll(specification, pageable);

    List<AppointmentResponse> appointments = appointmentsPage.getContent()
            .stream()
            .map(this::mapToAppointmentResponse)
            .toList();

    return new PageResponse<>(
            appointments,
            appointmentsPage.getNumber(),
            appointmentsPage.getTotalPages(),
            appointmentsPage.getTotalElements(),
            appointmentsPage.getSize(),
            appointmentsPage.hasNext(),
            appointmentsPage.hasPrevious(),
            appointmentsPage.isFirst(),
            appointmentsPage.isLast()
    );
}
```

## Exemplo de Requisições

### Buscar agendamentos de um usuário

```
GET /api/appointments/search?userId=123e4567-e89b-12d3-a456-426614174000&page=0&size=10
```

### Buscar agendamentos por status

```
GET /api/appointments/search?status=SCHEDULED&page=0&size=20&sortBy=createdAt&direction=DESC
```

### Buscar agendamentos de um serviço em uma data

```
GET /api/appointments/search?serviceId=123e4567-e89b-12d3-a456-426614174000&date=2024-03-20&page=0&size=10
```

### Buscar por código de confirmação

```java
@GetMapping("/code/{code}")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<AppointmentResponse> getAppointmentByCode(@PathVariable String code) {
    Specification<Appointment> specification = AppointmentSpecification.hasConfirmationCode(code);
    Appointment appointment = appointmentRepository.findOne(specification)
            .orElseThrow(() -> new IllegalArgumentException("Agendamento não encontrado"));
    return ResponseEntity.ok(mapToAppointmentResponse(appointment));
}
```

### Buscar agendamentos em um intervalo de datas

```java
@GetMapping("/search/range")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<PageResponse<AppointmentResponse>> searchAppointmentsByDateRange(
        @RequestParam(required = false) UUID userId,
        @RequestParam(required = false) AppointmentStatus status,
        @RequestParam(required = false) UUID serviceId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    Specification<Appointment> specification = AppointmentSpecification.withFiltersAndDateRange(
            userId, status, serviceId, startDate, endDate
    );

    Page<Appointment> appointmentsPage = appointmentRepository.findAll(specification, pageable);

    return ResponseEntity.ok(mapToPageResponse(appointmentsPage));
}
```

## Regra de Negócio - Segurança

Cidadãos (CITIZEN) só podem visualizar seus próprios agendamentos:

```java
@GetMapping
@PreAuthorize("hasAnyRole('ADMIN', 'CITIZEN')")
public ResponseEntity<PageResponse<AppointmentResponse>> getMyAppointments(
        @RequestParam(required = false) AppointmentStatus status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    UUID userId = getUserIdFromAuthentication(auth);

    boolean isAdmin = auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

    if (isAdmin) {
        return ResponseEntity.ok(appointmentService.searchAppointments(
                null, status, null, null, PageRequest.of(page, size)));
    }

    return ResponseEntity.ok(appointmentService.searchAppointments(
            userId, status, null, null, PageRequest.of(page, size)));
}
```

## Ordenação Recomendada

Para Appointments, as ordenações mais comuns são:

- `createdAt,DESC` - Agendamentos mais recentes
- `timeSlot.date,ASC` + `timeSlot.startTime,ASC` - Ordem cronológica do atendimento
- `status,ASC` - Agrupado por status

Para ordenação por data do timeSlot (requer join):

```java
Sort sort = Sort.by(
    Sort.Order.asc("timeSlot.date"),
    Sort.Order.asc("timeSlot.startTime")
);
Pageable pageable = PageRequest.of(page, size, sort);
```
