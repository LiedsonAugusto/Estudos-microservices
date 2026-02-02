# TimeSlot - Search e Paginação

## Especificações Disponíveis

O `TimeSlotSpecification` oferece os seguintes filtros:

### Filtros Básicos

- **serviceId**: Filtrar por serviço específico (UUID)
- **date**: Filtrar por data exata (LocalDate)
- **available**: Filtrar apenas horários disponíveis (bookedCount < capacity)
- **active**: Filtrar por status ativo/inativo

### Filtros Avançados

- **dateRange**: Filtrar por intervalo de datas (startDate e endDate)

## Exemplo de Uso no Controller

```java
@GetMapping("/search")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<PageResponse<TimeSlotResponse>> searchTimeSlots(
        @RequestParam(required = false) UUID serviceId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        @RequestParam(required = false) Boolean available,
        @RequestParam(required = false) Boolean active,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "date") String sortBy,
        @RequestParam(defaultValue = "ASC") String direction
) {
    Pageable pageable = createPageable(page, size, sortBy, direction);
    return ResponseEntity.ok(timeSlotService.searchTimeSlots(
            serviceId, date, available, active, pageable));
}
```

## Exemplo de Uso no Service

```java
public PageResponse<TimeSlotResponse> searchTimeSlots(UUID serviceId, LocalDate date,
                                                      Boolean available, Boolean active,
                                                      Pageable pageable) {
    Specification<TimeSlot> specification = TimeSlotSpecification.withFilters(
            serviceId, date, available, active
    );
    Page<TimeSlot> timeSlotsPage = timeSlotRepository.findAll(specification, pageable);

    List<TimeSlotResponse> timeSlots = timeSlotsPage.getContent()
            .stream()
            .map(this::mapToTimeSlotResponse)
            .toList();

    return new PageResponse<>(
            timeSlots,
            timeSlotsPage.getNumber(),
            timeSlotsPage.getTotalPages(),
            timeSlotsPage.getTotalElements(),
            timeSlotsPage.getSize(),
            timeSlotsPage.hasNext(),
            timeSlotsPage.hasPrevious(),
            timeSlotsPage.isFirst(),
            timeSlotsPage.isLast()
    );
}
```

## Exemplo de Requisições

### Buscar horários disponíveis de um serviço em uma data

```
GET /api/time-slots/search?serviceId=123e4567-e89b-12d3-a456-426614174000&date=2024-03-20&available=true&page=0&size=10
```

### Buscar todos os horários ativos de um serviço

```
GET /api/time-slots/search?serviceId=123e4567-e89b-12d3-a456-426614174000&active=true&page=0&size=20&sortBy=startTime&direction=ASC
```

### Buscar horários em um intervalo de datas

```java
@GetMapping("/search/range")
public ResponseEntity<PageResponse<TimeSlotResponse>> searchTimeSlotsByDateRange(
        @RequestParam(required = false) UUID serviceId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
        @RequestParam(required = false) Boolean available,
        @RequestParam(required = false) Boolean active,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "date", "startTime"));
    Specification<TimeSlot> specification = TimeSlotSpecification.withFiltersAndDateRange(
            serviceId, startDate, endDate, available, active
    );

    Page<TimeSlot> timeSlotsPage = timeSlotRepository.findAll(specification, pageable);

    return ResponseEntity.ok(mapToPageResponse(timeSlotsPage));
}
```

## Ordenação Recomendada

Para TimeSlots, as ordenações mais comuns são:

- `date,ASC` + `startTime,ASC` - Ordem cronológica
- `createdAt,DESC` - Horários criados recentemente
- `availableSpots,DESC` - Horários com mais vagas disponíveis

Para ordenação múltipla:

```java
Sort sort = Sort.by(
    Sort.Order.asc("date"),
    Sort.Order.asc("startTime")
);
Pageable pageable = PageRequest.of(page, size, sort);
```
