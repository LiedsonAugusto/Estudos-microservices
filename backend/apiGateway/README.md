# api-gateway

Ponto de entrada único da aplicação. Roteia requisições para os microserviços, aplica rate limiting por IP e configura CORS para o frontend.

---

## Configuração

O gateway usa `application.yml` em vez de `.properties`. As rotas já vêm configuradas para os serviços locais:

```yaml
server:
  port: 8080
```

---

## Rotas

| Padrão de rota | Serviço destino | Porta |
|---|---|---|
| `/api/users/**` | user-service | 8081 |
| `/api/auth/**` | user-service | 8081 |
| `/api/appointments/**` | scheduling-service | 8082 |
| `/api/services/**` | scheduling-service | 8082 |
| `/api/time-slots/**` | scheduling-service | 8082 |

> O email-service não possui rota pois não expõe endpoints REST — apenas consome mensagens do RabbitMQ.

---

## Rate Limiting

Implementado com **Bucket4j** + **Caffeine Cache** como filtro global do gateway.

| Configuração | Valor |
|---|---|
| Limite | 200 requisições por minuto por IP |
| Cache de IPs | Máximo 10.000 entradas, expiram após 5 minutos de inatividade |
| Detecção de IP | `X-Forwarded-For` (proxy) ou endereço remoto direto |
| Resposta ao exceder | HTTP 429 com JSON `{"error": "Too many requests..."}` |

---

## CORS

Configurado globalmente no `application.yml`:

| Configuração | Valor |
|---|---|
| Origem permitida | `http://localhost:3000` |
| Métodos | GET, POST, PUT, DELETE, OPTIONS |
| Headers | Todos (`*`) |
| Credentials | Habilitado |

---

## Tecnologias

| Dependência | Versão |
|---|---|
| Spring Boot | 4.0.3 |
| Spring Cloud Gateway | 2025.1.0 |
| Java | 21 |
| Bucket4j | 8.14.0 |
| Caffeine | gerenciado pelo Spring |

---

## Trabalhos futuros

- **Validação de JWT no gateway** — centralizar a autenticação no gateway em vez de cada serviço validar o token individualmente
- **Circuit breaker com Resilience4j** — lidar com falhas e indisponibilidade dos serviços downstream
- **Perfis por ambiente** — configurar origens CORS e URIs dos serviços por profile (dev, staging, prod)
- **Remoção de CORS duplicado** — remover configuração de CORS dos microserviços individuais, mantendo apenas no gateway
