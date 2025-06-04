# CodePipeline MCP Java Spring OAuth2

A professional Java Spring Boot microservice with OAuth2 authentication and WebSocket real-time capabilities following MCP (Model Context Protocol) design principles.

## Features

- **Spring Boot 3.2** with Java 17
- **OAuth2 Resource Server** with JWT validation
- **WebSocket** support with STOMP protocol
- **PostgreSQL** database with Spring Data JPA
- **OpenAPI 3.0** documentation with Swagger UI
- **Docker** and **Docker Compose** support
- **Actuator** for monitoring and management
- **TestContainers** for integration testing
- **Maven** build system
- **Git** version control

## Prerequisites

- Java 17 or later
- Maven 3.9+ or Gradle 8+
- Docker and Docker Compose (for containerized deployment)
- PostgreSQL 14+ (or use Docker)

## Getting Started

### Local Development

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/CodePipeline-MCP-JavaSpring-OAuth2.git
   cd CodePipeline-MCP-JavaSpring-OAuth2
   ```

2. Start the development environment:
   ```bash
   docker-compose up -d
   ```

3. Run the application:
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
   ```

4. Access the application:
   - API: http://localhost:8080/api
   - Swagger UI: http://localhost:8080/api/swagger-ui.html
   - H2 Console: http://localhost:8080/api/h2-console

### Environment Variables

Create a `.env` file in the project root with the following variables:

```env
# Database
DB_USERNAME=postgres
DB_PASSWORD=postgres
DB_NAME=mcp_db

# OAuth2
OAUTH2_ISSUER_URI=http://keycloak:8080/auth/realms/mcp
OAUTH2_JWK_SET_URI=http://keycloak:8080/auth/realms/mcp/protocol/openid-connect/certs
OAUTH2_AUDIENCE=mcp-api

# Application
SERVER_PORT=8080
SERVER_SERVLET_CONTEXT_PATH=/api
```

## Project Structure

```
src/
├── main/
│   ├── java/com/codepipeline/mcp/
│   │   ├── config/          # Configuration classes
│   │   ├── controller/      # REST controllers
│   │   ├── dto/            # Data Transfer Objects
│   │   ├── exception/      # Exception handling
│   │   ├── model/          # JPA entities
│   │   ├── repository/     # Spring Data repositories
│   │   ├── security/       # Security configurations
│   │   ├── service/        # Business logic
│   │   └── websocket/      # WebSocket configurations and controllers
│   └── resources/
│       ├── application.yml       # Main configuration
│       ├── application-dev.yml   # Development profile
│       └── application-prod.yml  # Production profile
└── test/                    # Test classes
```

## API Documentation

API documentation is available at runtime using Swagger UI:
- Swagger UI: http://localhost:8080/api/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api/v3/api-docs

## Testing

Run unit tests:
```bash
./mvnw test
```

Run integration tests (requires Docker):
```bash
./mvnw verify -Pintegration-test
```

## Building for Production

Build the application:
```bash
./mvnw clean package -DskipTests
```

## Docker

Build Docker image:
```bash
docker build -t codepipeline-mcp .
```

Run with Docker Compose:
```bash
docker-compose up -d
```

## Deployment

### Kubernetes

Deploy to Kubernetes:
```bash
kubectl apply -f k8s/
```

### Cloud Platforms

The application can be deployed to any cloud platform that supports Docker containers:
- AWS ECS/EKS
- Google Cloud Run/GKE
- Azure Container Apps/AKS
- Heroku

## Security

- OAuth2 with JWT tokens
- Role-based access control (RBAC)
- CSRF protection
- CORS configuration
- Secure headers
- Input validation

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Spring Security](https://spring.io/projects/spring-security)
- [Keycloak](https://www.keycloak.org/)
- [Docker](https://www.docker.com/)
- [Kubernetes](https://kubernetes.io/)
 CodePipeline-MCP-JavaSpring-OAuth2
