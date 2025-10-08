# Payment Service

Payment Service is one of the microservices in the Scalable E-Commerce Platform architecture. The service is responsible for handling payments in the e-commerce system.

## Features

- **Payment Creation** - generating payment links for orders
- **Payment Processing** - simulating successful payments
- **Status Management** - tracking payment status (PENDING, PAID, EXPIRED, CANCELLED, FAILED)
- **Automatic Expiration** - Quartz Scheduler automatically expires payments after 10 minutes
- **Outbox Pattern** - preparation for communication with other services
- **Swagger** - documentation

## Architecture

### Entities

- **Payment** - main payment entity
- **PaymentEvent** - payment events (Outbox Pattern)

### Payment Statuses

- `PENDING` - waiting for payment
- `PAID` - payment completed
- `EXPIRED` - payment expired
- `CANCELLED` - payment cancelled
- `FAILED` - payment failed

### API Endpoints

- `POST /api/v1/payments` - create new payment
- `GET /api/v1/payments/{id}` - get payment by ID
- `GET /api/v1/payments/order/{orderId}` - get payment by order ID
- `GET /api/v1/payments/user/{userId}` - get user payments
- `POST /api/v1/payments/process/{paymentLink}` - process payment
- `POST /api/v1/payments/expire` - manually expire payments

## Configuration

### Database

The service requires PostgreSQL. Configuration in `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/payment_service
spring.datasource.username=postgres
spring.datasource.password=postgres
```

### Port

The service runs on port `8081`.

### Quartz Scheduler

The service uses Quartz Scheduler for automatic payment expiration:

- **Schedule**: Every 10 minutes (13:00, 13:10, 13:20, 13:30, ...)
- **Job**: PaymentExpirationJob - expires payments with status PENDING
- **Store**: In-memory (simple configuration)
- **Start**: From the next full minute after application startup

## Setup and Running

### 1. Docker

Start PostgreSQL with Docker:

```bash
docker run -d \
  --name payment-service-postgres \
  -e POSTGRES_DB=payment_service \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:15
```

### 2. Compile and Run

```bash
# Compile the project
./mvnw clean compile

# Run the application
./mvnw spring-boot:run
```

## Project Structure

```
src/main/java/com/ecmsp/paymentservice/
├── api/
│   ├── kafka/
│   │   ├── KafkaPaymentEventPublisher.java
│   │   ├── PaymentEventPublisher.java
│   │   ├── PaymentProcessedKafkaEventFailed.java
│   │   ├── PaymentProcessedKafkaEventSucceeded.java
│   │   ├── PaymentRequestedKafkaConsumer.java
│   │   └── PaymentRequestedKafkaEvent.java
│   └── rest/
│       ├── health/
│       │   └── HealthController.java
│       └── payment/
│           ├── dto/
│           │   ├── CreatePaymentRequest.java
│           │   └── PaymentResponse.java
│           └── PaymentController.java
├── aplication/
│   └── config/
│       └── KafkaConfiguration.java
├── payment/
│   ├── adapter/
│   │   ├── db/
│   │   │   ├── PaymentEntity.java
│   │   │   └── PaymentEventEntity.java
│   │   ├── job/
│   │   │   └── PaymentExpirationJob.java
│   │   ├── repository/
│   │   │   ├── PaymentEventRepository.java
│   │   │   └── PaymentRepository.java
│   │   └── service/
│   │       └── PaymentService.java
│   ├── config/
│   │   ├── OpenApiConfiguration.java
│   │   └── QuartzConfiguration.java
│   └── domain/
│       ├── ClientId.java
│       ├── Currency.java
│       ├── OrderId.java
│       ├── PaymentState.java
│       └── PaymentToCreate.java
└── PaymentServiceApplication.java
```

## Database Schema

The service automatically creates the following tables:

### payments

- `id` - primary key
- `order_id` - order identifier
- `user_id` - user identifier
- `amount` - payment amount
- `currency` - payment currency
- `status` - payment status
- `payment_link` - unique payment link
- `expires_at` - payment expiration time
- `paid_at` - payment completion time
- `created_at` - creation timestamp
- `updated_at` - last update timestamp
- `version` - optimistic locking version

### payment_events

- `id` - primary key
- `payment_id` - reference to payment
- `event_type` - type of event
- `event_data` - event data (JSON)
- `status` - event processing status
- `retry_count` - retry attempts
- `processed_at` - processing timestamp
- `created_at` - creation timestamp

## Scheduled Jobs

### PaymentExpirationJob

- **Purpose**: Automatically expires payments that have passed their expiration time
- **Schedule**: Every 10 minutes
- **Logic**: Finds payments with status PENDING and expires_at <= current time
- **Actions**:
  - Changes status to EXPIRED
  - Creates PaymentEvent for Outbox Pattern
  - Logs expiration details
