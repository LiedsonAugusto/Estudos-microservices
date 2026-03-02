# user-service

Responsável pelo cadastro, autenticação e gerenciamento de usuários. Publica eventos no RabbitMQ quando um novo usuário é registrado.

---

## Configuração

Copie o arquivo de exemplo e preencha com suas credenciais:

```bash
cp src/main/resources/application.properties.example \
   src/main/resources/application.properties
```

Variáveis necessárias:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/users_db
spring.datasource.username=
spring.datasource.password=

jwt.secret=
jwt.expiration=86400000

spring.rabbitmq.addresses=
rabbitmq.exchange.user=user.exchange
rabbitmq.routingkey.user.created=user.created
```

---

## Endpoints

### Autenticação (público)

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/auth/register` | Cadastrar novo usuário |
| `POST` | `/api/auth/login` | Login, retorna JWT |

### Usuário logado

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/api/users/me` | Retorna dados do usuário autenticado |
| `PUT` | `/api/users/me` | Atualiza nome e telefone |

### Administração (ADMIN)

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/api/users` | Lista todos os usuários |
| `GET` | `/api/users/paginated` | Lista com paginação |
| `GET` | `/api/users/search` | Busca por nome, email ou CPF |
| `GET` | `/api/users/{id}` | Busca usuário por ID |
| `PUT` | `/api/users/{id}/status` | Ativa ou desativa usuário |

---

## Eventos publicados

| Exchange | Routing Key | Quando |
|---|---|---|
| `user.exchange` | `user.created` | Novo usuário registrado |
