# scheduling-service

Responsável pelo gerenciamento de serviços públicos, horários disponíveis e agendamentos. Publica eventos no RabbitMQ nas ações de criação e cancelamento de agendamentos.

---

## Configuração

Copie o arquivo de exemplo e preencha com suas credenciais:

```bash
cp src/main/resources/application.properties.example \
   src/main/resources/application.properties
```

Variáveis necessárias:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/schedulings_db
spring.datasource.username=
spring.datasource.password=

jwt.secret=
jwt.expiration=86400000

spring.rabbitmq.addresses=
rabbitmq.exchange.scheduling=scheduling.exchange
rabbitmq.queue.email=email.queue
rabbitmq.routingkey.appointment.created=appointment.created
rabbitmq.routingkey.appointment.cancelled=appointment.cancelled
rabbitmq.routingkey.appointment.reminder=appointment.reminder
```

> O `jwt.secret` deve ser o mesmo usado no user-service.

---

## Endpoints

### Serviços (ADMIN)

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/services` | Criar serviço |
| `GET` | `/api/services` | Listar serviços |
| `GET` | `/api/services/{id}` | Buscar serviço por ID |
| `PUT` | `/api/services/{id}` | Atualizar serviço |
| `DELETE` | `/api/services/{id}` | Desativar serviço |

### Horários (ADMIN)

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/time-slots` | Criar horário |
| `POST` | `/api/time-slots/batch` | Criar horários em lote |
| `GET` | `/api/time-slots` | Listar horários (filtros: serviceId, date, available) |
| `GET` | `/api/time-slots/{id}` | Buscar horário por ID |
| `PUT` | `/api/time-slots/{id}` | Atualizar horário |
| `DELETE` | `/api/time-slots/{id}` | Desativar horário |

### Agendamentos

| Método | Endpoint | Permissão | Descrição |
|---|---|---|---|
| `POST` | `/api/appointments` | CITIZEN | Criar agendamento |
| `GET` | `/api/appointments` | CITIZEN/ADMIN | Listar agendamentos |
| `GET` | `/api/appointments/{id}` | CITIZEN/ADMIN | Buscar por ID |
| `GET` | `/api/appointments/confirmation/{code}` | CITIZEN/ADMIN | Buscar por código de confirmação |
| `PUT` | `/api/appointments/{id}/cancel` | CITIZEN/ADMIN | Cancelar agendamento |
| `PUT` | `/api/appointments/{id}/confirm` | ADMIN | Check-in do cidadão |
| `PUT` | `/api/appointments/{id}/complete` | ADMIN | Marcar como atendido |
| `PUT` | `/api/appointments/{id}/no-show` | ADMIN | Marcar não comparecimento |

---

## Eventos publicados

| Exchange | Routing Key | Quando |
|---|---|---|
| `scheduling.exchange` | `appointment.created` | Agendamento criado |
| `scheduling.exchange` | `appointment.cancelled` | Agendamento cancelado |
| `scheduling.exchange` | `appointment.reminder` | Lembrete 24h antes (scheduler) |
