# Padrão de Paginação

## PageResponse

O projeto utiliza um DTO customizado `PageResponse<T>` para retornar dados paginados:

```java
public record PageResponse<T>(
    List<T> content,
    int currentPage,
    int totalPages,
    long totalElements,
    int pageSize,
    boolean hasNext,
    boolean hasPrevious,
    boolean isFirst,
    boolean isLast
) {}
```

## Parâmetros Padrão

Todos os endpoints paginados seguem os mesmos parâmetros de query:

- `page` (default: 0) - Número da página (zero-indexed)
- `size` (default: 10) - Tamanho da página (min: 1, max: 100)
- `sortBy` (default: varia por endpoint) - Campo para ordenação
- `direction` (default: DESC ou ASC) - Direção da ordenação

## Método Auxiliar createPageable

```java
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
```

## Conversão de Page para PageResponse

Método helper para converter `Page<Entity>` em `PageResponse<DTO>`:

```java
private <T, R> PageResponse<R> mapToPageResponse(Page<T> page, Function<T, R> mapper) {
    List<R> content = page.getContent()
            .stream()
            .map(mapper)
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
```

## Exemplo de Uso Completo

### Controller

```java
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
```

### Service

```java
public PageResponse<ServiceResponse> getAllServicesPaginated(Pageable pageable) {
    Page<Services> servicePage = serviceRepository.findAll(pageable);

    List<ServiceResponse> listServices = servicePage.getContent()
            .stream()
            .map(this::mapToServiceResponse)
            .toList();

    return new PageResponse<>(
            listServices,
            servicePage.getNumber(),
            servicePage.getTotalPages(),
            servicePage.getTotalElements(),
            servicePage.getSize(),
            servicePage.hasNext(),
            servicePage.hasPrevious(),
            servicePage.isFirst(),
            servicePage.isLast()
    );
}
```

## Exemplo de Requisições

### Primeira página com 10 itens

```
GET /api/services/paginated
GET /api/services/paginated?page=0&size=10
```

### Segunda página com 20 itens, ordenado por nome

```
GET /api/services/paginated?page=1&size=20&sortBy=name&direction=ASC
```

### Todos os itens ordenados por data de criação (mais recentes primeiro)

```
GET /api/services/paginated?page=0&size=100&sortBy=createdAt&direction=DESC
```

## Resposta JSON

```json
{
  "content": [
    {
      "id": "123e4567-e89b-12d3-a456-426614174000",
      "name": "Renovação de CNH",
      "description": "Serviço de renovação de CNH",
      "durationMinutes": 30,
      "active": true
    }
  ],
  "currentPage": 0,
  "totalPages": 5,
  "totalElements": 50,
  "pageSize": 10,
  "hasNext": true,
  "hasPrevious": false,
  "isFirst": true,
  "isLast": false
}
```

## Paginação com Specifications

Para endpoints com filtros de busca, combine Pageable com Specification:

```java
@GetMapping("/search")
public ResponseEntity<PageResponse<ServiceResponse>> searchServices(
        @RequestParam(required = false) String name,
        @RequestParam(required = false) Boolean active,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @RequestParam(defaultValue = "DESC") String direction
) {
    Pageable pageable = createPageable(page, size, sortBy, direction);
    Specification<Services> specification = ServicesSpecification.withFilters(name, null, null, active);
    Page<Services> servicesPage = serviceRepository.findAll(specification, pageable);

    return ResponseEntity.ok(mapToPageResponse(servicesPage));
}
```

## Boas Práticas

1. **Validação de Parâmetros**: Sempre valide `page` >= 0 e `size` entre 1 e 100
2. **Default Values**: Forneça valores padrão sensatos para todos os parâmetros
3. **Ordenação Padrão**: Escolha uma ordenação padrão que faça sentido para o contexto
4. **Campos de Ordenação**: Valide se o campo de ordenação existe na entidade (opcional)
5. **Performance**: Para tabelas grandes, considere índices nos campos de ordenação
6. **Documentação**: Documente os campos disponíveis para ordenação no Swagger/OpenAPI

## Múltiplos Campos de Ordenação

Para ordenar por múltiplos campos:

```java
Sort sort = Sort.by(
    Sort.Order.asc("date"),
    Sort.Order.asc("startTime"),
    Sort.Order.desc("createdAt")
);
Pageable pageable = PageRequest.of(page, size, sort);
```

Ou via query string (se implementado):

```
GET /api/time-slots/paginated?page=0&size=10&sort=date,asc&sort=startTime,asc
```
