### Builder Image
FROM maven:latest AS build
WORKDIR /build
COPY . .
RUN mvn clean install -Dmaven.test.skip=true

####APP Image
FROM alpine:latest
RUN apk update && apk add openjdk21-jdk
COPY --from=build /build/target/*.jar ./app.jar
ENTRYPOINT ["java","-jar", "app.jar"]
