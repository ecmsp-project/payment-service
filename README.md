# Payment Service

Payment Service is one of the microservices in the Scalable E-Commerce Platform architecture. The service is responsible for handling payments in the e-commerce system.

## Features

- **Payment Creation** - generating payment links for orders
- **Payment Processing** - simulating successful payments
- **Status Management** - tracking payment status (PENDING, PAID, EXPIRED, CANCELLED, FAILED)
- **Outbox Pattern** - preparation for communication with other services
- **REST API** - comprehensive API for payment operations
- **Swagger Documentation** - interactive API documentation

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
├── controller/
│   └── PaymentController.java
├── dto/
│   ├── CreatePaymentRequest.java
│   └── PaymentResponse.java
├── entity/
│   ├── Payment.java
│   ├── PaymentEvent.java
│   ├── PaymentStatus.java
│   ├── PaymentEventType.java
│   └── EventStatus.java
├── exception/
│   └── GlobalExceptionHandler.java
├── repository/
│   ├── PaymentRepository.java
│   └── PaymentEventRepository.java
├── service/
│   └── PaymentService.java
└── config/
    └── OpenApiConfig.java
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

