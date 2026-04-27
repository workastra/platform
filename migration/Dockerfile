# syntax=docker/dockerfile:1.19.0

# Build stage
FROM ghcr.io/graalvm/native-image-community:25.0.1 AS build

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
    ./gradlew --no-daemon migration:nativeCompile

# Runtime stage
FROM gcr.io/distroless/base-debian12:nonroot

WORKDIR /app

# Copy native executable
COPY --from=build /app/migration/build/native/nativeCompile/migration ./migration

CMD ["/app/migration"]
