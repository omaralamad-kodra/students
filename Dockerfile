####APP Image
FROM alpine:latest
RUN apk update && apk add openjdk21-jdk
COPY target/*.jar /app.jar
ENTRYPOINT ["java","-jar", "app.jar"]
