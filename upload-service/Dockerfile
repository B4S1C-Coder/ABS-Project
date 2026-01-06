FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app
COPY pom.xml .
COPY upload-service/pom.xml upload-service/pom.xml

RUN mvn -pl upload-service -am dependency:go-offline -B

COPY upload-service/src upload-service/src
RUN mvn -pl upload-service -am clean package -DskipTests=true


FROM gcr.io/distroless/java21-debian12:latest AS runner

WORKDIR /app
COPY --from=builder ./app/target/upload-service-0.0.1-SNAPSHOT.jar ./app.jar

EXPOSE 20001
ENTRYPOINT ["java", "-jar", "app.jar"]