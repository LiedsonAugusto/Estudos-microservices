# Sistema de Agendamento de Serviços Públicos

Projeto de estudo com foco em arquitetura de microserviços, comunicação assíncrona com RabbitMQ e autenticação JWT.

O sistema permite que cidadãos agendem atendimentos presenciais em um órgão público e recebam notificações por email. Administradores gerenciam serviços, horários e agendamentos.

---

## Arquitetura

```
┌─────────────────────────────────────────────────────┐
│               Frontend (Next.js 14)                  │
│               http://localhost:3000                  │
└────────────────────────┬────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────┐
│               API Gateway (porta 8080)               │
│          Roteamento, JWT, CORS, Rate Limiting        │
└──────────┬──────────────────────────┬───────────────┘
           │                          │
           ▼                          ▼
┌──────────────────┐      ┌──────────────────────┐
│  user-service    │      │  scheduling-service   │
│  porta 8081      │      │  porta 8082           │
│  PostgreSQL 5432 │      │  PostgreSQL 5432      │
└────────┬─────────┘      └──────────┬────────────┘
         │                           │
         │      publica eventos      │
         └────────────┬──────────────┘
                      ▼
          ┌────────────────────────┐
          │        RabbitMQ        │
          │   user.exchange        │
          │   scheduling.exchange  │
          │   email.queue          │
          └───────────┬────────────┘
                      │ consome eventos
                      ▼
          ┌────────────────────────┐
          │     email-service      │
          │     porta 8083         │
          │     Gmail SMTP         │
          └────────────────────────┘
```

> API Gateway e Frontend ainda não implementados.

---

## Serviços

| Serviço | Porta | Descrição |
|---|---|---|
| [user-service](./backend/userService/README.md) | 8081 | Cadastro, autenticação JWT e gerenciamento de usuários |
| [scheduling-service](./backend/schedulingService/README.md) | 8082 | Serviços, horários e agendamentos |
| [email-service](./backend/emailService/README.md) | 8083 | Consome eventos do RabbitMQ e envia emails |

---

## Pré-requisitos

- Java 21
- Maven 3.8+
- PostgreSQL 16
- Conta no [CloudAMQP](https://www.cloudamqp.com/) (plano gratuito disponível)

---

## Como rodar

Cada serviço tem seu próprio `application.properties`. Copie o arquivo de exemplo e configure com suas credenciais:

```bash
cp src/main/resources/application.properties.example \
   src/main/resources/application.properties
```

Depois execute:

```bash
./mvnw spring-boot:run
```

Consulte o README de cada serviço para detalhes específicos de configuração.
