FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

# All the POMs
COPY pom.xml .
COPY common/pom.xml common/pom.xml
COPY upload-service/pom.xml upload-service/pom.xml
COPY play-service/pom.xml play-service/pom.xml
COPY video-processing-service/pom.xml video-processing-service/pom.xml

RUN mvn -pl play-service -am dependency:go-offline -B

# Only the required source folders
COPY common/src common/src
COPY play-service/src play-service/src

RUN mvn -pl play-service -am clean package -DskipTests=true

FROM gcr.io/distroless/java21-debian12:latest AS runner

WORKDIR /app
COPY --from=builder /app/play-service/target/play-service-0.0.1-SNAPSHOT.jar app.jar


EXPOSE 20020
ENTRYPOINT ["java", "-jar", "app.jar"]