FROM amazoncorretto:17

WORKDIR /app

ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

EXPOSE 8083

ENTRYPOINT ["java","-jar","app.jar"]