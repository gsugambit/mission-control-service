FROM eclipse-temurin:21.0.10_7-jdk AS test

# 1. Language/Locale Settings
ENV LANG='en_US.UTF-8' LANGUAGE='en_US:en' LC_ALL='en_US.UTF-8'

ENV TZ=Etc/UTC
ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    findutils \
    tzdata \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

ARG GIT_BRANCH
ARG IMAGE_TAG

COPY ./  /app

RUN ./gradlew test --stacktrace

FROM eclipse-temurin:21.0.10_7-jdk AS builder

# 1. Language/Locale Settings
ENV LANG='en_US.UTF-8' LANGUAGE='en_US:en' LC_ALL='en_US.UTF-8'

ENV TZ=Etc/UTC
ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    findutils \
    tzdata \
    && rm -rf /var/lib/apt/lists/* \

WORKDIR /app

ARG GIT_BRANCH
ARG IMAGE_TAG

COPY . /app

RUN ./gradlew bootJar -Pversion=${IMAGE_TAG} --stacktrace -x test

FROM eclipse-temurin:21.0.10_7-jdk AS build
ENV PORT=8080
EXPOSE 8080

# 1. Language/Locale Settings
ENV LANG='en_US.UTF-8' LANGUAGE='en_US:en' LC_ALL='en_US.UTF-8'

ENV TZ=Etc/UTC
ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    findutils \
    tzdata \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=builder /app/build/libs/*.jar /app/app.jar

LABEL org.opencontainers.image.title="Base JDK 21 Runner"
LABEL org.opencontainers.image.description="Eclipse Temurin 21 JDK, findutils, UTC Timezone"
LABEL org.opencontainers.image.vendor="Gambit Labs"
LABEL org.opencontainers.image.base.name="eclipse-temurin:21.0.8_9-jdk"
LABEL org.opencontainers.image.version="21.0.10_7-v1"
LABEL org.opencontainers.image.authors="GSUGambit <gsugambit@gsugambit.com>"

CMD exec java ${JAVA_USER_OPTS} -jar app.jar