FROM eclipse-temurin:25-jdk-alpine AS builder
WORKDIR /app
COPY gradlew gradlew.bat ./
COPY gradle ./gradle
COPY build.gradle settings.gradle ./
COPY src ./src
RUN ./gradlew bootJar -x test --no-daemon

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
ENV JAVA_TOOL_OPTIONS="-Xmx256m -Xms128m"
ENTRYPOINT ["java", "-jar", "app.jar"]
