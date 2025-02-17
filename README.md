# User Registration API

This is a Spring Boot application that provides APIs for user registration and management.

## Features

- User registration with name, email, gender, and password
- IP address and country tracking
- User authentication using email and password
- JWT-based Authentication
- Admin access to view all users
- Admin access to delete users
- Swagger API documentation
- Dockerized application

## Prerequisites

- Java 11 or higher
- Maven
- Docker (for containerization)

## Getting Started

### Building the Application

```bash
mvn clean package
```

### Running the Application

#### Using Maven

```bash
mvn spring-boot:run
```

#### Using Java

```bash
java -jar target/user-registration-0.0.1-SNAPSHOT.jar
```

#### Using Docker

Build the Docker image:
```bash
docker build -t user-registration-api .
```

Run the Docker container:
```bash
docker run -p 8080:8080 user-registration-api
```

## API Documentation

Swagger UI is available at: [http://localhost:9090/swagger-ui/](http://localhost:9090/swagger-ui/)

## API Endpoints

### Public Endpoints

- **POST** `/user/register` - Register a new user
- **POST** `/user/login` - Authenticate and retrieve JWT token

### Admin-only Endpoints

- **GET** `/user/getAllUsers?requestingUserEmail={email}` - Get all registered users
- **DELETE** `/user/deleteUser/{userId}?requestingUserEmail={email}` - Delete a user by ID

## Default Admin Account

- Email: `admin@mykarehealth.com`
- Password: `admin123`

## Testing

Run the JUnit tests:
```bash
mvn test
```

## Notes

- The application uses an in-memory H2 database for development.
- The H2 console is available at: [http://localhost:9090/h2-console](http://localhost:9090/h2-console)
- Database credentials:
  - JDBC URL: `jdbc:h2:mem:testdb`
  - Username: `sa`
  - Password: `password`

