# Otel Rezervasyon Sistemi - User Guide

This guide provides instructions on how to run and use the Hotel Reservation System.

## System Overview

The system consists of four microservices:

1. **Hotel Service**: Manages hotels and rooms
2. **Reservation Service**: Handles reservation operations
3. **Notification Service**: Processes reservation notifications via Kafka
4. **API Gateway**: Provides a single entry point with JWT authentication

## Prerequisites

- Docker and Docker Compose
- Java 17 (for local development)
- Maven (for local development)

## Running the Application

### Using Docker Compose

1. Clone the repository
2. Navigate to the project root directory
3. Run the following command:

```bash
docker-compose up -d
```

This will start all the services, including:

- PostgreSQL databases (separate instances for hotel and reservation services)
- Zookeeper and Kafka
- Hotel Service
- Reservation Service
- Notification Service
- API Gateway

The system uses health checks to ensure services start in the correct order:
- Databases start first
- Kafka and Zookeeper start next
- Microservices start after their dependencies are healthy

To check the status of all containers:
```bash
docker-compose ps
```

To view logs from all services:
```bash
docker-compose logs -f
```

To view logs from a specific service:
```bash
docker-compose logs -f <service-name>
```

To stop all services:
```bash
docker-compose down
```

To stop all services and remove volumes (this will delete all data):
```bash
docker-compose down -v
```

### Accessing the Services

- API Gateway: http://localhost:8000
- Hotel Service: http://localhost:8080
- Reservation Service: http://localhost:8081
- Notification Service: http://localhost:8082

## API Documentation

### Authentication

To access protected endpoints, you need to authenticate and obtain a JWT token:

```
POST /api/auth/login
```

Request body:

```json
{
  "username": "user",
  "password": "password"
}
```

Response:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

Use this token in the Authorization header for subsequent requests:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### Hotel Service

#### Hotels

- `GET /api/hotels` - Get all hotels
- `GET /api/hotels/{id}` - Get a specific hotel
- `POST /api/hotels` - Create a new hotel
- `PUT /api/hotels/{id}` - Update a hotel
- `DELETE /api/hotels/{id}` - Delete a hotel

#### Rooms

- `GET /api/rooms` - Get all rooms
- `GET /api/rooms/{id}` - Get a specific room
- `GET /api/rooms/hotel/{hotelId}` - Get rooms by hotel ID
- `POST /api/rooms` - Create a new room
- `PUT /api/rooms/{id}` - Update a room
- `DELETE /api/rooms/{id}` - Delete a room

### Reservation Service

- `GET /api/reservations` - Get all reservations (requires authentication)
- `GET /api/reservations/{id}` - Get a specific reservation (requires authentication)
- `GET /api/reservations/hotel/{hotelId}` - Get reservations by hotel ID (requires authentication)
- `GET /api/reservations/room/{roomId}` - Get reservations by room ID (requires authentication)
- `POST /api/reservations` - Create a new reservation (requires authentication)
- `PUT /api/reservations/{id}` - Update a reservation (requires authentication)
- `DELETE /api/reservations/{id}` - Delete a reservation (requires authentication)

## Swagger-UI links

All services provide Swagger UI for API documentation. You can access them directly or through the API Gateway:

### Direct Access
- Hotel Service API: http://localhost:8080/swagger-ui.html
- Reservation Service API: http://localhost:8081/swagger-ui.html
- Notification Service API: http://localhost:8082/swagger-ui.html

### Through API Gateway
- API Gateway Swagger UI (aggregated): http://localhost:8000/swagger-ui.html
- Hotel Service API: http://localhost:8000/hotel-service/swagger-ui.html
- Reservation Service API: http://localhost:8000/reservation-service/swagger-ui.html
- Notification Service API: http://localhost:8000/notification-service/swagger-ui.html

## Sample Requests

### Creating a Hotel

```
POST /api/hotels
```

Request body:

```json
{
  "name": "Grand Hotel",
  "address": "123 Main St, City",
  "starRating": 5
}
```

### Creating a Room

```
POST /api/rooms
```

Request body:

```json
{
  "hotelId": 1,
  "roomNumber": "101",
  "capacity": 2,
  "pricePerNight": 150.00
}
```

### Making a Reservation

```
POST /api/reservations
```

Request body:

```json
{
  "hotelId": 1,
  "roomId": 1,
  "guestName": "John Doe",
  "checkInDate": "2023-12-01",
  "checkOutDate": "2023-12-05"
}
```

## Security

- Public endpoints: Hotel and room information
- Protected endpoints: All reservation operations (require JWT authentication)
- Admin users have access to all endpoints
- Regular users can only access their own reservations
- Passwords are securely hashed using BCrypt before storage
- JWT tokens are signed with a secret key and include user roles

## Event-Driven Architecture

When a reservation is created, a Kafka event is published to the `reservation-created` topic. The Notification Service
consumes this event and processes it (in this implementation, it logs the notification details).

## Test Users

The system comes with two predefined users:

1. Admin user:
    - Username: admin
    - Password: password (stored securely using BCrypt hashing)
    - Roles: ROLE_ADMIN

2. Regular user:
    - Username: user
    - Password: password (stored securely using BCrypt hashing)
    - Roles: ROLE_USER

Note: The passwords shown above are the plain text versions. In the system, all passwords are securely hashed using
BCrypt before storage.

## Troubleshooting

If you encounter any issues:

1. Check that all services are running:
   ```bash
   docker-compose ps
   ```

2. Check the logs for any errors:
   ```bash
   docker-compose logs [service-name]
   ```

3. Ensure that Kafka and Zookeeper are running properly:
   ```bash
   docker-compose logs kafka
   docker-compose logs zookeeper
   ```

4. Verify that the databases are accessible:
   ```bash
   docker-compose exec hotel-db psql -U postgres -d hotel_service
   docker-compose exec reservation-db psql -U postgres -d reservation_service
   ```

5. Common issues and solutions:

   - **Services fail to start**: Check if the dependent services (databases, Kafka) are healthy. The docker-compose file includes health checks to ensure proper startup order.

   - **Database connection issues**: Verify that the environment variables in the docker-compose.yml file match the application configuration. The services are configured to use the container names as hostnames.

   - **Kafka connection issues**: If services can't connect to Kafka, check if the Kafka container is healthy and if the bootstrap servers configuration is correct.

   - **API Gateway routing issues**: Ensure all services are running and accessible from the API Gateway container. The gateway is configured to use service names as hostnames.

6. Rebuilding services after code changes:
   ```bash
   docker-compose build [service-name]
   docker-compose up -d [service-name]
   ```
