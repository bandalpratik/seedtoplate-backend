FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw
RUN ./mvnw -q dependency:go-offline

COPY src ./src
RUN ./mvnw -q -DskipTests package

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

RUN groupadd --system spring && useradd --system --gid spring --create-home spring

COPY --from=build /workspace/target/*.jar /app/app.jar

ENV TZ=UTC \
    SERVER_PORT=8080 \
    SPRING_DATASOURCE_URL=jdbc:postgresql://ep-rough-snow-b5vtcmlh-pooler.c-7.us-east-2.aws.neon.tech/neondb?sslmode=require&channelBinding=require \
    SPRING_DATASOURCE_USERNAME=neondb_owner \
    SPRING_DATASOURCE_PASSWORD=npg_jWgbnrAFa21m \
    JWT_SECRET=seedtoplate-dev-secret-change-me \
    JWT_EXPIRATION_MS=86400000

EXPOSE 8080
USER spring:spring

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
