# email-service

Consome eventos do RabbitMQ e envia emails transacionais via Gmail SMTP. Não expõe endpoints REST.

---

## Configuração

Copie o arquivo de exemplo e preencha com suas credenciais:

```bash
cp src/main/resources/application.properties.example \
   src/main/resources/application.properties
```

Variáveis necessárias:

```properties
spring.rabbitmq.addresses=

rabbitmq.exchange.user=user.exchange
rabbitmq.exchange.appointment=scheduling.exchange
rabbitmq.queue.email=email.queue
rabbitmq.routingkey.user.created=user.created
rabbitmq.routingkey.appointment.created=appointment.created
rabbitmq.routingkey.appointment.cancelled=appointment.cancelled
rabbitmq.routingkey.appointment.reminder=appointment.reminder

spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=
spring.mail.password=
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

> Para o Gmail, use uma **senha de app**, não sua senha normal.
> Como gerar: [support.google.com/accounts/answer/185833](https://support.google.com/accounts/answer/185833)

---

## Eventos consumidos

| Routing Key | Email enviado |
|---|---|
| `user.created` | Boas-vindas ao novo usuário |
| `appointment.created` | Confirmação de agendamento com código |
| `appointment.cancelled` | Notificação de cancelamento com motivo |
| `appointment.reminder` | Lembrete 24h antes do agendamento |

Todos os eventos são roteados para a mesma fila (`email.queue`). O Spring identifica qual método consumidor chamar pelo tipo do parâmetro do evento.
