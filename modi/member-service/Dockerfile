FROM amazoncorretto:17

WORKDIR /app

ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

EXPOSE 8085

ENTRYPOINT ["java","-jar","app.jar"]