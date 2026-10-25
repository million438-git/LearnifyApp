# --- STAGE 1: Build Application ---
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace

# Copy Maven wrapper and POM
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x ./mvnw && ./mvnw dependency:go-offline -B

# Copy backend source code
COPY src/ src/

# Build optimized production JAR without running integration tests
RUN ./mvnw clean package -DskipTests -B

# --- STAGE 2: Lightweight Production Runtime ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Security: Run as dedicated non-root service user
RUN addgroup -S learnify && adduser -S learnify -G learnify
USER learnify:learnify

# Copy executable jar from builder stage
COPY --from=builder --chown=learnify:learnify /workspace/target/studentbackend-0.0.1-SNAPSHOT.jar app.jar

# Cloud Run injects $PORT (default 8080)
ENV PORT=8080
EXPOSE 8080

# Health check probe against operational health endpoint
HEALTHCHECK --interval=30s --timeout=5s --start-period=15s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:${PORT}/api/health || exit 1

# Launch application with memory limits
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]

