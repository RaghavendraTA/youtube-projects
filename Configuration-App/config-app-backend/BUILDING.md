# Building and Deploying Configuration App Backend

## Build Instructions

### Prerequisites
- Java 21 JDK
- Maven 3.9.6+
- Docker (optional, for containerized builds)

### Building with Maven

```bash
# Navigate to the backend directory
cd config-app-backend

# Clean and build
mvn clean package

# Build without running tests
mvn clean package -DskipTests

# Build with specific Java version
mvn clean package -Djava.version=21
```

The built JAR file will be available at: `target/configuration-app-backend-1.0.0.jar`

### Building Docker Image

```bash
# Build with Docker
docker build -t configapp-backend:1.0.0 .

# Tag for registry
docker tag configapp-backend:1.0.0 your-registry/configapp-backend:1.0.0

# Push to registry
docker push your-registry/configapp-backend:1.0.0
```

## Running the Application

### Using Maven
```bash
mvn spring-boot:run
```

### Using JAR
```bash
java -XX:+UseVirtualThreads -jar target/configuration-app-backend-1.0.0.jar
```

### Using Docker
```bash
docker run -p 8080:8080 \
  -e SPRING_DATA_MONGODB_URI=mongodb://localhost:27017 \
  -e JWT_SECRET=your-secret-key \
  configapp-backend:1.0.0
```

### Using Docker Compose
```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f config-app-backend

# Stop services
docker-compose down

# Remove volumes (careful!)
docker-compose down -v
```

## Environment Variables

Key environment variables for deployment:

| Variable | Default | Description |
|----------|---------|-------------|
| SPRING_DATA_MONGODB_URI | mongodb://localhost:27017 | MongoDB connection string |
| SPRING_DATA_MONGODB_DATABASE | configapp | Database name |
| JWT_SECRET | (set in properties) | JWT signing secret |
| JWT_EXPIRATION | 3600000 | Token expiry in ms (1 hour) |
| JWT_REFRESH_EXPIRATION | 604800000 | Refresh token expiry in ms (7 days) |
| SERVER_PORT | 8080 | Application port |
| JAVA_OPTS | -XX:+UseVirtualThreads | JVM options |

## Testing

### Run All Tests
```bash
mvn test
```

### Run Specific Test Class
```bash
mvn test -Dtest=AdminServiceTest
```

### Run with Coverage
```bash
mvn test jacoco:report
# Report available at: target/site/jacoco/index.html
```

## Production Deployment

### Kubernetes Deployment

Example manifest for Kubernetes (requires Dockerfile and service setup):

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: configapp-backend
spec:
  replicas: 3
  selector:
    matchLabels:
      app: configapp-backend
  template:
    metadata:
      labels:
        app: configapp-backend
    spec:
      containers:
      - name: configapp-backend
        image: your-registry/configapp-backend:1.0.0
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_DATA_MONGODB_URI
          valueFrom:
            secretKeyRef:
              name: configapp-secrets
              key: mongodb-uri
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: configapp-secrets
              key: jwt-secret
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 10
          periodSeconds: 5
```

### Azure Container Apps Deployment

```bash
# Create resource group
az group create --name configapp-rg --location eastus

# Create container registry
az acr create --resource-group configapp-rg \
  --name configappcr --sku Basic

# Build and push image
az acr build --registry configappcr \
  --image configapp-backend:latest .

# Deploy to Container Apps
az containerapp create \
  --name configapp-backend \
  --resource-group configapp-rg \
  --image configappcr.azurecr.io/configapp-backend:latest \
  --environment myenv \
  --target-port 8080 \
  --ingress external \
  --cpu 0.5 \
  --memory 1.0Gi
```

### Performance Tuning

JVM Options for Production:
```bash
JAVA_OPTS="-XX:+UseVirtualThreads \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -Xms512m \
  -Xmx2g"
```

## Security Checklist

- [ ] Change JWT secret to a strong value (minimum 256 bits)
- [ ] Update MongoDB credentials
- [ ] Enable HTTPS/TLS in production
- [ ] Set up proper network security groups/firewall rules
- [ ] Use secret management (e.g., Azure Key Vault, AWS Secrets Manager)
- [ ] Enable audit logging
- [ ] Regular security patches and dependency updates
- [ ] Implement rate limiting
- [ ] Set up monitoring and alerting

## Monitoring

### Application Health Check
```bash
curl http://localhost:8080/actuator/health
```

### Logging
- Logs are output to console by default
- Configure log aggregation (e.g., ELK, Splunk) in production
- Log level can be adjusted via `logging.level.*` in application.properties

### Metrics
Spring Boot Actuator endpoints (if enabled):
- `/actuator/health` - Application health
- `/actuator/metrics` - Application metrics
- `/actuator/env` - Environment properties

## Troubleshooting

### MongoDB Connection Issues
```bash
# Test MongoDB connection
mongo "mongodb://localhost:27017/configapp"

# Check logs for connection errors
docker-compose logs mongodb
```

### Port Already in Use
```bash
# Change the port in application.properties
server.port=8081

# Or for Docker
docker run -p 8081:8080 configapp-backend:1.0.0
```

### JWT Token Issues
- Verify JWT_SECRET is set correctly
- Check token expiration time
- Ensure Authorization header format: `Bearer <token>`

## CI/CD Integration

### GitHub Actions Example
```yaml
name: Build and Deploy

on: [push]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - uses: actions/setup-java@v2
        with:
          java-version: '21'
      - run: mvn clean package
      - uses: docker/build-push-action@v2
        with:
          push: true
          tags: myregistry/configapp-backend:${{ github.sha }}
```
