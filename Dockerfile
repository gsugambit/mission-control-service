# syntax=docker/dockerfile:1

# 1. Base stage for shared configuration
FROM eclipse-temurin:21.0.10_7-jdk AS base
ENV LANG='en_US.UTF-8' LANGUAGE='en_US:en' LC_ALL='en_US.UTF-8' \
    TZ=Etc/UTC \
    DEBIAN_FRONTEND=noninteractive
    
RUN apt-get update && \
    apt-get install -y --no-install-recommends findutils tzdata && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

# 2. Dependencies stage (to cache downloaded jars)
FROM base AS deps
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
# Use a mount cache for the gradle home to persist downloads
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew help --no-daemon

# 3. Builder stage
FROM deps AS builder
ARG IMAGE_TAG
COPY . .
# Use mount caches for both the gradle home and the build output
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew bootJar -Pversion=${IMAGE_TAG} --stacktrace -x test --no-daemon

# 4. Final runtime stage
FROM base AS build
ENV PORT=8080
EXPOSE 8080

COPY --from=builder /app/build/libs/*.jar /app/app.jar

LABEL org.opencontainers.image.title="Mission Control Service"
LABEL org.opencontainers.image.vendor="Gambit Labs"

CMD ["java", "-jar", "app.jar"]