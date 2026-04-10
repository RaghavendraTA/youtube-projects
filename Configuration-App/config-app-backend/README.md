# Configuration App - Backend

A centralized configuration management service built with Spring Boot 3.2 and MongoDB. This backend provides REST APIs for administrative configuration management and consumer service lookups.

## Features

- **Admin Management**: User registration, login, and profile management with JWT authentication
- **Configuration Management**: Create, read, update, delete configurations with version history
- **Configuration Stats**: Track lookup statistics for each configuration
- **Consumer Lookup**: Non-human service lookup endpoints with settings by GUID or key
- **Async Operations**: Non-blocking I/O operations using Spring `@Async` and virtual threads
- **Security**: JWT-based authentication with refresh token support
- **Error Handling**: Comprehensive exception handling with proper HTTP status codes

## Technology Stack

- **Language**: Java 21
- **Framework**: Spring Boot 3.2.0
- **Database**: MongoDB
- **Authentication**: JWT (JSON Web Tokens)
- **Async Processing**: Spring @Async with virtual threads
- **Build**: Maven 3.9.6
- **Docker**: Containerized deployment

## Prerequisites

- Java 21 or higher
- Maven 3.9.6+
- Docker & Docker Compose (for containerized setup)

## Local Setup

### Option 1: Using Docker Compose (Recommended)

```bash
# Navigate to the project root
cd config-app-backend

# Start all services (MongoDB + Backend)
docker-compose up --build

# The API will be available at http://localhost:8080
```

### Option 2: Manual Setup

1. **Install MongoDB**
   - Download and install MongoDB locally
   - Default connection: `mongodb://localhost:27017`

2. **Update Configuration**
   - Edit `src/main/resources/application.properties`
   - Update MongoDB connection string if needed

3. **Build the Project**
   ```bash
   mvn clean install
   ```

4. **Run the Application**
   ```bash
   mvn spring-boot:run
   ```

The API will be available at `http://localhost:8080`

## API Endpoints

### Authentication APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/admin/register` | Register new user |
| POST | `/api/v1/admin/login` | Login user |
| POST | `/api/v1/admin/refresh` | Refresh JWT token |
| GET | `/api/v1/admin/{id}` | Get admin details |
| PATCH | `/api/v1/admin/{id}` | Update admin profile |

### Configuration APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/configuration/create` | Create new configuration |
| GET | `/api/v1/configuration` | List user's configurations |
| GET | `/api/v1/configuration/history/{id}` | Get configuration history |
| GET | `/api/v1/configuration/stats/{id}` | Get configuration stats |
| PATCH | `/api/v1/configuration/{id}` | Update configuration |
| PUT | `/api/v1/configuration/{version}/{id}` | Activate specific version |
| DELETE | `/api/v1/configuration/{id}` | Delete configuration |
| PUT | `/api/v1/configuration/transfer/{id}` | Transfer ownership |

### Lookup APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/lookup/{configurationId}` | Lookup settings by GUID or key |

## Authentication

All endpoints (except `/admin/register`, `/admin/login`, `/admin/refresh`, and `/lookup/**`) require JWT authentication.

**Request Header:**
```
Authorization: Bearer <jwt-token>
```

## Configuration File

Edit `application.properties` to customize:

```properties
# MongoDB
spring.data.mongodb.uri=mongodb://localhost:27017
spring.data.mongodb.database=configapp

# JWT
jwt.secret=your-secret-key
jwt.expiration=3600000
jwt.refresh-expiration=604800000

# Server
server.port=8080
```

## Building the Docker Image

```bash
docker build -t configapp-backend:1.0.0 .
```

## Running Tests

```bash
mvn test
```

## Project Structure

```
config-app-backend/
├── src/main/java/com/configapp/
│   ├── ConfigurationAppApplication.java
│   ├── controller/          # REST Controllers
│   ├── service/             # Business Logic
│   ├── repository/          # Data Access Layer
│   ├── model/               # Domain Models
│   ├── dto/                 # Data Transfer Objects
│   ├── security/            # Security & JWT
│   └── exception/           # Exception Handling
├── src/main/resources/
│   └── application.properties
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── README.md
```

## Error Handling

All errors are returned with appropriate HTTP status codes and JSON responses:

```json
{
  "timestamp": "2024-03-21T10:30:00",
  "status": 400,
  "error": "Invalid request",
  "message": "Details about the error"
}
```

## Security Notes

- **Change JWT Secret**: Update `jwt.secret` in `application.properties` with a secure key (minimum 256 bits)
- **Change MongoDB Password**: Update credentials in Docker Compose and application properties
- **Use HTTPS**: Deploy with HTTPS in production
- **Secret Management**: Use environment variables or secret management systems in production

## Development

### Enable Debug Logging
```properties
logging.level.com.configapp=DEBUG
```

### Virtual Threads
The application uses Java 21 virtual threads for efficient async operations. Set JVM option:
```
-XX:+UseVirtualThreads
```

## Support

For issues or questions, please refer to the requirements document: `backend-ai-doc.md`
