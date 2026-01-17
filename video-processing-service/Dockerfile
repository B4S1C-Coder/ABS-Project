FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

## All the POMs
COPY pom.xml .
COPY common/pom.xml common/pom.xml
COPY upload-service/pom.xml upload-service/pom.xml
COPY play-service/pom.xml play-service/pom.xml
COPY video-processing-service/pom.xml video-processing-service/pom.xml

RUN mvn -pl video-processing-service -am dependency:go-offline -B

# Only the required source folders
COPY common/src common/src
COPY video-processing-service/src video-processing-service/src

RUN mvn -pl video-processing-service -am clean package -DskipTests=true

# Runtime must be GPU Compatible
FROM nvidia/cuda:12.4.1-runtime-ubuntu22.04 AS runner
ENV DEBIAN_FRONTEND=noninteractive

# Install FFmpeg CLI
RUN apt-get update \
 && apt-get install -y --no-install-recommends \
    openjdk-21-jre-headless \
    ffmpeg \
    ca-certificates \
 && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY --from=builder /app/video-processing-service/target/video-processing-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 20009
ENTRYPOINT ["java", "-jar", "app.jar"]