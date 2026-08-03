# Omnify

Omnichannel E-commerce Management System (OMS/ERP) — Modular Monolith với DDD strict isolation,
sẵn sàng tách microservices (`sync-engine`, `inventory`, `order-oms`) khi cần scale.

## Tech Stack

- **Backend**: Java 21, Spring Boot 3.3.x
- **Database**: PostgreSQL 16 + Flyway migrations
- **Cache / Lock**: Redis + Redisson (distributed lock)
- **Messaging**: RabbitMQ (transactional queue) + Kafka (event streaming)
- **Search**: Elasticsearch
- **Resilience**: Resilience4j (Circuit Breaker, Rate Limiter, Retry với exponential backoff)
- **Security**: Spring Security + JWT (access token) / opaque refresh token
- **API Docs**: springdoc-openapi (Swagger UI)

## Cấu trúc module (bounded context)

```
com.omnify
├── common               // shared kernel: config, exception, security
├── auth                 // authentication, RBAC, session management
├── masterdata
├── product               // SPU/SKU, marketplace mapping
├── inventory              // stock sync, buffer, distributed lock
├── pricingmarketing
├── orderoms
├── shippinglogistics
├── returnrefund
├── syncengine             // queue, retry, idempotency
└── channelintegration     // Shopee / Lazada / TikTok Shop adapters
```

Mỗi module tuân theo layer chuẩn: `controller -> service -> repository -> domain (entity) -> dto`.
Module không được truy cập trực tiếp vào `repository`/`domain` của module khác — chỉ được gọi qua
`service` public API hoặc domain event, xem chi tiết trong `package-info.java` của từng module.

## Chạy local

### 1. Khởi động hạ tầng (Postgres, Redis, RabbitMQ, Elasticsearch)

```bash
docker compose up -d
```

### 2. Chạy ứng dụng (profile `dev` mặc định — tự seed data mẫu)

```bash
./mvnw spring-boot:run
```

Flyway sẽ tự động chạy `db/migration/V1__init_schema.sql` (schema) và, ở profile `dev`,
thêm `db/seed/V2__seed_sample_data.sql` (dữ liệu mẫu). Profile `prod` **không** load seed data.

### 3. Kiểm tra

- API: http://localhost:8080/api/v1/auth/login
- Swagger UI: http://localhost:8080/swagger-ui.html
- Health check: http://localhost:8080/actuator/health
- RabbitMQ management: http://localhost:15672 (guest/guest)

## Biến môi trường quan trọng (production)

| Biến | Mô tả | Mặc định (dev) |
|---|---|---|
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | Kết nối PostgreSQL | localhost:5432/omnify |
| `REDIS_HOST`, `REDIS_PORT` | Kết nối Redis | localhost:6379 |
| `RABBITMQ_HOST` | Kết nối RabbitMQ | localhost |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka brokers | localhost:9092 |
| `ELASTICSEARCH_URIS` | Elasticsearch | http://localhost:9200 |
| `JWT_SECRET` | **Bắt buộc đổi** khi lên production | dev-only default |
| `SPRING_PROFILES_ACTIVE` | `dev` \| `prod` | dev |

## Việc cần làm tiếp (chưa implement trong scaffold này)

- [ ] `JwtAuthenticationFilter` để parse JWT từ header và set `SecurityContext`
- [ ] Đổi hash thuật toán refresh token từ MD5 sang SHA-256 (xem TODO trong `RefreshTokenService`)
- [ ] RBAC method-level authorization (`@PreAuthorize`) dựa trên bảng `permissions`
- [ ] Module `product`, `inventory`, `orderoms`... hiện chỉ có `package-info.java` khai báo ranh giới,
      chưa có entity/logic — implement dần theo yêu cầu nghiệp vụ cụ thể.
- [ ] `sync-engine`: hàng đợi + idempotency key + retry cho đồng bộ 2 chiều với sàn TMĐT.
