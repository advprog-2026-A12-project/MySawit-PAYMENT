FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle

RUN chmod +x gradlew && ./gradlew dependencies --no-daemon

COPY src ./src
COPY config ./config

RUN ./gradlew bootJar --no-daemon -x test
RUN JAR_FILE=$(ls build/libs/*.jar | grep -v -- '-plain\.jar$' | head -n 1) && cp "$JAR_FILE" app.jar

FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

COPY --from=builder /app/app.jar app.jar

EXPOSE 8002

ENTRYPOINT ["java", "-jar", "app.jar"]
