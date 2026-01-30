# 🔐 User Service

Microserviço de gerenciamento de usuários, autenticação e autorização para o Sistema de Agendamento de Serviços Públicos.

---

## 📋 Sobre o Projeto

O **User Service** é responsável por todo o ciclo de vida dos usuários no sistema, incluindo cadastro, autenticação JWT, gerenciamento de perfis e publicação de eventos relacionados a usuários.

Este serviço faz parte de uma arquitetura de microserviços orientada a eventos, utilizando RabbitMQ para comunicação assíncrona.

### ✨ Funcionalidades Principais

- ✅ Cadastro de novos usuários (cidadãos e administradores)
- ✅ Autenticação com JWT (JSON Web Token)
- ✅ Gerenciamento de perfis de usuário
- ✅ Validação de CPF brasileiro
- ✅ Controle de acesso baseado em roles (RBAC)
- ✅ Publicação de eventos no RabbitMQ
- ✅ Paginação e filtros avançados para listagem
- ✅ Rate limiting para proteção de endpoints

---

## 🛠️ Tecnologias Utilizadas

| Tecnologia | Versão | Descrição |
|------------|--------|-----------|
| **Java** | 21 | Linguagem de programação |
| **Spring Boot** | 4.0.1 | Framework principal |
| **Spring Security** | - | Autenticação e autorização |
| **JWT (jjwt)** | 0.12.5 | Geração e validação de tokens |
| **Spring Data JPA** | - | Persistência de dados |
| **PostgreSQL** | 16 | Banco de dados relacional |
| **RabbitMQ** | - | Message broker para eventos |
| **Bucket4j** | 8.10.1 | Rate limiting |
| **Lombok** | - | Redução de boilerplate |
| **Maven** | - | Gerenciamento de dependências |

---

## 🏗️ Arquitetura

```
┌─────────────────────────────────────────────────────────┐
│                    USER SERVICE                          │
│                    Port: 8081                            │
│                                                          │
│  ┌──────────────┐    ┌──────────────┐                   │
│  │ Controllers  │───▶│  Services    │                   │
│  └──────────────┘    └──────────────┘                   │
│         │                    │                           │
│         │                    ▼                           │
│         │            ┌──────────────┐                   │
│         │            │ Repositories │                   │
│         │            └──────────────┘                   │
│         │                    │                           │
│         ▼                    ▼                           │
│  ┌──────────────┐    ┌──────────────┐                   │
│  │   Security   │    │  PostgreSQL  │                   │
│  │  (JWT/Auth)  │    │  users_db    │                   │
│  └──────────────┘    └──────────────┘                   │
│         │                                                │
│         └────────────┐                                   │
│                      ▼                                   │
│               ┌──────────────┐                          │
│               │   Producer   │                          │
│               └──────────────┘                          │
└─────────────────────┼────────────────────────────────────┘
                      │
                      ▼
              ┌──────────────┐
              │   RabbitMQ   │
              │user.exchange │
              └──────────────┘
```

---

## 🚀 Como Rodar

### Pré-requisitos

- Java 21 ou superior
- PostgreSQL 16
- Maven 3.8+
- Conta no CloudAMQP (ou RabbitMQ local)

### 1. Configurar o Banco de Dados

```bash
# Criar banco de dados PostgreSQL
createdb users_db
```

### 2. Configurar Variáveis de Ambiente

Edite `src/main/resources/application.properties`:

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/users_db
spring.datasource.username=seu_usuario
spring.datasource.password=sua_senha

# RabbitMQ
spring.rabbitmq.addresses=sua_url_cloudamqp

# JWT Secret (gere uma chave segura)
jwt.secret=sua_chave_secreta_base64
```

### 3. Executar o Projeto

```bash
# Compilar
./mvnw clean install

# Rodar
./mvnw spring-boot:run
```

O serviço estará disponível em: `http://localhost:8081`

---

## 📡 Endpoints da API

### Autenticação (Público)

#### Registrar Novo Usuário
```http
POST /api/auth/register
Content-Type: application/json

{
  "name": "João Silva",
  "email": "joao@example.com",
  "cpf": "12345678901",
  "password": "senha123",
  "phone": "5583999999999"
}
```

**Resposta:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "user": {
    "id": "uuid",
    "name": "João Silva",
    "email": "joao@example.com",
    "cpf": "12345678901",
    "phone": "5583999999999",
    "role": "CITIZEN",
    "createdAt": "2025-01-27T10:30:00"
  }
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "joao@example.com",
  "password": "senha123"
}
```

---

### Usuários (Autenticado)

#### Obter Dados do Usuário Logado
```http
GET /api/users/me
Authorization: Bearer {token}
```

#### Atualizar Próprios Dados
```http
PUT /api/users/me
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "João Silva Santos",
  "phone": "5583988888888"
}
```

---

### Administração (ADMIN apenas)

#### Listar Todos os Usuários
```http
GET /api/users
Authorization: Bearer {token}
```

#### Listar com Paginação
```http
GET /api/users/paginated?page=0&size=10&sortBy=createdAt&direction=DESC
Authorization: Bearer {token}
```

#### Buscar Usuários com Filtros
```http
GET /api/users/search?name=João&email=example.com&page=0&size=10
Authorization: Bearer {token}
```

#### Obter Usuário por ID
```http
GET /api/users/{id}
Authorization: Bearer {token}
```

#### Ativar/Desativar Usuário
```http
PUT /api/users/{id}/status
Authorization: Bearer {token}
```

---

## 📨 Eventos RabbitMQ

### Exchange Criado

- **Nome:** `user.exchange`
- **Tipo:** `topic`
- **Durável:** `true`

### Eventos Publicados

#### UserCreatedEvent

**Routing Key:** `user.created`

**Payload:**
```json
{
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "name": "João Silva",
  "email": "joao@example.com",
  "timestamp": "2025-01-27T10:30:00"
}
```

**Quando é publicado:**
- Sempre que um novo usuário se registra no sistema

**Consumidores esperados:**
- `email-service`: Envia email de boas-vindas

---

## ⚙️ Configuração

### application.properties

```properties
# Aplicação
spring.application.name=userService
server.port=8081

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/users_db
spring.datasource.username=postgres
spring.datasource.password=senha

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# JWT
jwt.secret=sua_chave_base64
jwt.expiration=86400000

# RabbitMQ
spring.rabbitmq.addresses=amqps://usuario:senha@servidor.com/vhost
rabbitmq.exchange.user=user.exchange
rabbitmq.routingkey.user.created=user.created
```

---

## 📂 Estrutura do Projeto

```
src/main/java/com/estudo/userService/
├── configs/
│   ├── RabbitMQConfig.java          # Configuração do RabbitMQ
│   └── SecurityConfig.java          # Configuração de segurança
├── controllers/
│   ├── AuthController.java          # Endpoints de autenticação
│   └── UserController.java          # Endpoints de usuários
├── dtos/
│   ├── AuthResponse.java            # Response de autenticação
│   ├── LoginRequest.java            # Request de login
│   ├── RegisterRequest.java         # Request de registro
│   ├── UserCreatedEvent.java        # Evento publicado no RabbitMQ
│   ├── UserResponse.java            # Response de usuário
│   └── UserUpdateRequest.java       # Request de atualização
├── entities/
│   └── User.java                    # Entidade JPA
├── enums/
│   └── UserRole.java                # CITIZEN, ADMIN
├── exceptions/
│   └── GlobalExceptionHandler.java  # Tratamento global de erros
├── producers/
│   └── UserProducer.java            # Publicador de eventos RabbitMQ
├── repository/
│   └── UserRepository.java          # Repositório JPA
├── security/
│   ├── CustomUserDetails.java       # UserDetails customizado
│   ├── CustomUserDetailsService.java
│   ├── JwtAuthenticationFilter.java # Filtro JWT
│   ├── JwtService.java              # Serviço JWT
│   └── RateLimitFilter.java         # Rate limiting
├── services/
│   ├── AuthService.java             # Lógica de autenticação
│   └── UserService.java             # Lógica de usuários
├── specifications/
│   └── UserSpecification.java       # Filtros dinâmicos
└── validators/
    ├── CPFValidator.java            # Validador de CPF
    └── ValidCPF.java                # Anotação de validação
```

---

## 🔒 Segurança

### Autenticação JWT

- Tokens válidos por 24 horas (configurável)
- Secret key deve ser mantida em segredo (usar variáveis de ambiente em produção)
- Algoritmo: HS256 (HMAC with SHA-256)

### Roles e Permissões

| Role | Permissões |
|------|------------|
| **CITIZEN** | Visualizar e editar próprio perfil |
| **ADMIN** | Todas as permissões + gerenciar usuários |

### Rate Limiting

- Proteção contra força bruta
- Configurado com Bucket4j
- Filtro aplicado globalmente

### Validações

#### CPF
- Validação matemática dos dígitos verificadores
- Rejeita CPFs conhecidos como inválidos (111.111.111-11, etc)

#### Email
- Validação de formato usando Bean Validation

#### Senha
- Mínimo: 8 caracteres
- Máximo: 22 caracteres
- Hash: BCrypt

---

## 🚧 Trabalhos Futuros

### 🔴 Alta Prioridade

#### 1. Implementar Refresh Token
**Status:** ❌ Não implementado
**Descrição:** Adicionar mecanismo de refresh token para renovação automática de tokens JWT sem novo login.

**Tarefas:**
- [ ] Adicionar campo `refreshToken` no `AuthResponse`
- [ ] Criar endpoint `POST /api/auth/refresh`
- [ ] Implementar lógica de geração e validação de refresh token
- [ ] Armazenar refresh tokens (em banco ou cache)
- [ ] Implementar revogação de tokens

**Benefícios:**
- Melhor experiência do usuário (não precisa fazer login frequentemente)
- Mais segurança (access token com tempo curto de vida)
- Conformidade com OAuth 2.0 best practices

---

#### 2. Migrar de DDL-auto para Flyway
**Status:** ⚠️ Usando `hibernate.ddl-auto=update`
**Descrição:** Substituir geração automática de schema por migrations versionadas com Flyway.

**Tarefas:**
- [ ] Adicionar dependência do Flyway no `pom.xml`
- [ ] Criar pasta `src/main/resources/db/migration/`
- [ ] Gerar migration inicial (V1__create_users_table.sql)
- [ ] Alterar `spring.jpa.hibernate.ddl-auto` para `validate`
- [ ] Documentar processo de criação de novas migrations

**Benefícios:**
- Controle de versão do schema do banco
- Rastreabilidade de mudanças
- Facilita deploy em múltiplos ambientes
- Previne perda acidental de dados

**Exemplo de Migration:**
```sql
-- V1__create_users_table.sql
CREATE TABLE users (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    cpf VARCHAR(11) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    active BOOLEAN NOT NULL DEFAULT true
);
```

---

#### 3. Implementar Testes Automatizados
**Status:** ❌ Apenas teste básico do Spring Initializr
**Descrição:** Criar suite completa de testes unitários e de integração.

**Tarefas:**
- [ ] **Testes Unitários:**
  - [ ] AuthService (register, login)
  - [ ] UserService (CRUD, filtros)
  - [ ] JwtService (geração, validação)
  - [ ] CPFValidator
  - [ ] UserProducer (mock do RabbitMQ)

- [ ] **Testes de Integração:**
  - [ ] Controllers (MockMvc)
  - [ ] Repository (Testcontainers PostgreSQL)
  - [ ] RabbitMQ (Testcontainers RabbitMQ)
  - [ ] Segurança (endpoints protegidos)

- [ ] **Configuração:**
  - [ ] Adicionar JUnit 5
  - [ ] Adicionar Mockito
  - [ ] Adicionar Testcontainers
  - [ ] Configurar cobertura de código (Jacoco)

**Meta:** Mínimo 80% de cobertura de código

---

### 🟡 Média Prioridade

#### 4. Adicionar Swagger/OpenAPI
**Status:** ❌ Não implementado
**Descrição:** Documentação interativa da API com Swagger UI.

**Tarefas:**
- [ ] Adicionar dependência `springdoc-openapi-starter-webmvc-ui`
- [ ] Configurar informações da API (título, versão, descrição)
- [ ] Adicionar anotações nos endpoints (@Operation, @ApiResponse)
- [ ] Configurar esquemas de segurança (JWT)
- [ ] Documentar DTOs com @Schema

**Acesso:** `http://localhost:8081/swagger-ui.html`

---

#### 5. Implementar Eventos Adicionais
**Status:** ❌ Apenas `user.created` implementado
**Descrição:** Publicar eventos para outras ações do ciclo de vida do usuário.

**Eventos a implementar:**
- [ ] `user.updated` - Quando dados do usuário são atualizados
- [ ] `user.deleted` - Quando usuário é desativado
- [ ] `user.password.changed` - Quando senha é alterada
- [ ] `user.role.changed` - Quando role é modificado (ADMIN)

**Routing Keys:**
```properties
rabbitmq.routingkey.user.updated=user.updated
rabbitmq.routingkey.user.deleted=user.deleted
rabbitmq.routingkey.user.password.changed=user.password.changed
rabbitmq.routingkey.user.role.changed=user.role.changed
```

---

#### 6. Docker e Docker Compose
**Status:** ❌ Não configurado
**Descrição:** Containerizar o serviço e criar docker-compose para ambiente local.

**Tarefas:**
- [ ] Criar `Dockerfile` multi-stage build
- [ ] Criar `docker-compose.yml` com:
  - [ ] PostgreSQL (porta 5433 conforme spec)
  - [ ] RabbitMQ com Management UI
  - [ ] User Service
- [ ] Configurar variáveis de ambiente
- [ ] Documentar comandos no README

**Benefícios:**
- Fácil setup para novos desenvolvedores
- Ambiente consistente
- Facilita testes de integração

---

### 🟢 Baixa Prioridade

#### 7. Logs Estruturados
**Status:** ⚠️ Logs básicos
**Descrição:** Implementar logging estruturado com contexto.

**Tarefas:**
- [ ] Adicionar SLF4J + Logback
- [ ] Configurar formato JSON para logs
- [ ] Adicionar MDC (Mapped Diagnostic Context) com:
  - [ ] Request ID
  - [ ] User ID
  - [ ] Correlation ID
- [ ] Log de eventos importantes (login, registro, erros)

---

#### 8. Monitoramento e Métricas
**Status:** ❌ Não implementado
**Descrição:** Adicionar observabilidade ao serviço.

**Tarefas:**
- [ ] Adicionar Spring Boot Actuator
- [ ] Configurar endpoints de health check
- [ ] Expor métricas Prometheus
- [ ] Adicionar métricas customizadas:
  - [ ] Registros por minuto
  - [ ] Logins por minuto
  - [ ] Eventos publicados no RabbitMQ

---

#### 9. Auditoria
**Status:** ❌ Não implementado
**Descrição:** Rastrear mudanças em entidades para fins de auditoria.

**Tarefas:**
- [ ] Implementar `@EntityListeners` do JPA
- [ ] Criar tabela de auditoria
- [ ] Registrar quem/quando fez mudanças
- [ ] Endpoint para consultar histórico (ADMIN)

---

#### 10. Melhorias de Segurança
**Status:** ⚠️ Segurança básica implementada

**Tarefas:**
- [ ] Implementar proteção CSRF (se necessário)
- [ ] Adicionar header de segurança (helmet)
- [ ] Implementar account lockout (bloqueio após X tentativas)
- [ ] Adicionar CORS configurável por ambiente
- [ ] Implementar 2FA (Two-Factor Authentication)
- [ ] Adicionar audit log de acessos sensíveis

---

## 🤝 Microserviços Relacionados

Este serviço faz parte do ecossistema:

- **api-gateway** (Port 8080): Roteamento e gateway
- **scheduling-service** (Port 8082): Gerenciamento de agendamentos
- **email-service** (Port 8083): Envio de emails

---

## 📄 Licença

Este é um projeto de estudos.

---

## 👤 Autor

**Liedson** - Estudando microserviços e arquitetura distribuída

---

## 📚 Referências

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [RabbitMQ Tutorials](https://www.rabbitmq.com/getstarted.html)
- [JWT.io](https://jwt.io/)
- [Spring Security](https://spring.io/projects/spring-security)
- [Flyway Documentation](https://flywaydb.org/documentation)
- [Testcontainers](https://www.testcontainers.org/)
