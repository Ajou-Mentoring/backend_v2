# Dockerfile
FROM openjdk:21
WORKDIR /app
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar
RUN ls -al /app
ENTRYPOINT ["java","-jar","app.jar"]