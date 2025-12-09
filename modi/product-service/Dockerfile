FROM amazoncorretto:17

WORKDIR /app

# Actions에서 이미 빌드된 jar만 복사
COPY build/libs/product-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java","-jar","/app/app.jar"]
