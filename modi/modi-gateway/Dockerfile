# syntax=docker/dockerfile:1.6
FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app

# 0) Gradle이 쓸 캐시 경로(지금 이미지에서 HOME=/root 인 경우가 많음)
ARG MODULE=modi-gateway
ENV GRADLE_USER_HOME=/root/.gradle-${MODULE}

# 1) 캐시가 잘 먹게 빌드 스크립트/래퍼 먼저 복사
COPY gradlew .
COPY gradle gradle
COPY settings.gradle* build.gradle* gradle.properties* ./

RUN chmod +x gradlew

# 2) (옵션) 의존성 캐시 워밍업 - 처음 1회만 느리고 이후 엄청 빨라짐
RUN --mount=type=cache,target=/root/.gradle-${MODULE} \
    --mount=type=cache,id=gradle-wrapper,target=/app/.gradle-wrapper \
    ./gradlew --no-daemon --console=plain help

# 3) 이제 소스 복사
COPY . .

# 4) 메모리/병렬 제한으로 OOM 방지
ENV GRADLE_OPTS="-Dorg.gradle.jvmargs=-Xmx512m -XX:MaxMetaspaceSize=256m -Dfile.encoding=UTF-8"

RUN --mount=type=cache,target=/root/.gradle-${MODULE} \
    --mount=type=cache,id=gradle-wrapper,target=/app/.gradle-wrapper \
    ./gradlew :${MODULE}:bootJar -x test --no-daemon --console=plain --no-parallel --max-workers=1

# ===== run stage =====
FROM eclipse-temurin:17-jre
WORKDIR /app
ARG MODULE=modi-gateway
COPY --from=builder /app/${MODULE}/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
