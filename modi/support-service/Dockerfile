# syntax=docker/dockerfile:1.6
FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app

ENV GRADLE_USER_HOME=/root/.gradle

COPY gradlew .
COPY gradle gradle
COPY settings.gradle* build.gradle* gradle.properties* ./

RUN chmod +x gradlew

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew --no-daemon --console=plain help

COPY . .

ARG MODULE=support-service
ENV GRADLE_OPTS="-Dorg.gradle.jvmargs=-Xmx512m -XX:MaxMetaspaceSize=256m -Dfile.encoding=UTF-8"

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew :${MODULE}:bootJar -x test --no-daemon --console=plain --no-parallel --max-workers=1

FROM eclipse-temurin:17-jre
WORKDIR /app
ARG MODULE=support-service
COPY --from=builder /app/${MODULE}/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
