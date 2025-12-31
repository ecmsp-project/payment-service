# Payment-service


## Local developemnt with kafka

1. Run docker-compose.yml to run db and kafka broker
2. Run payment service with `dev` active profile

### Access Kafka UI

To access Kafka UI, open the browser and navigate to `http://localhost:8088`.

### Publishing test message to Kafka topic via Kafka UI

To publish a test message to Kafka topic via Kafka UI, follow these steps:

1. Open Kafka UI in your browser at `http://localhost:8088`.
2. Select the Kafka cluster you want to interact with.
3. Select `Topics` from the navigation panel on the left.
4. Select the given topic you want to publish a message to.
5. Click on the `Produce Message` button.
6. In the `Key` field, enter the key identifier for your message
7. In the `Value` field, enter the message content in JSON format. Example"

8. In payload section add for payment-processed-failed or payment-processed-succeeded. 
    These events are produced by this service and should be received by order-service (also with `dev` profile active)
```json
    {
      "orderId": "550e8400-e29b-41d4-a716-446655440001",
      "paymentId": "b526bf0b-da2e-40ba-8062-22d7132e4d95",
      "processedAt": "2025-10-25T15:06:06"
    }
```


