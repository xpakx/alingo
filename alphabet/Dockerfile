# Build stage
FROM maven:3.8.1-openjdk-17-slim AS build
WORKDIR /app
COPY . /app
RUN mvn clean package -DskipTests

# Run stage
FROM openjdk:17-alpine
MAINTAINER xpakx.github.io
EXPOSE 8081
COPY --from=build app/target/alingo-0.0.1-SNAPSHOT.jar application.jar
ENTRYPOINT ["java","-jar","/application.jar"]
RUN apk --no-cache add curl
