# syntax=docker/dockerfile:1.19.0

# Build stage
FROM ghcr.io/graalvm/jdk-community:25.0.1 AS build

WORKDIR /app

# Copy build files first for better layer caching
COPY gradlew gradlew.bat settings.gradle.kts build.gradle.kts ./
COPY gradle/ gradle/

# Download Gradle distribution
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew --version

# Copy source and build
COPY core/ core/
COPY console/ console/
COPY iam/ iam/
COPY migration/ migration/

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew --no-daemon iam:bootJar

# Runtime stage
FROM eclipse-temurin:25-jre

WORKDIR /app

COPY --from=build /app/iam/build/libs/iam-*.jar iam.jar

USER 1000

EXPOSE 9000

CMD ["java", "-jar", "/app/iam.jar"]
