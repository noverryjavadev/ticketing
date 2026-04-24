# Gunakan base image JDK yang ringan (Alpine version)
FROM maven:3.9.6-eclipse-temurin-21 AS build

COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source code dan build project
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime stage menggunakan JRE 21 yang ringan (Alpine Linux)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy hasil build dari stage 1
COPY --from=build /app/target/*.jar app.jar

# Expose port aplikasi (sesuaikan jika kamu ganti server.port di application.properties)
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]