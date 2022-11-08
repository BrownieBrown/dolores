# syntax=docker/dockerfile:1
FROM gradle:7.5.1-jdk17 AS build

COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle buildFatJar --no-daemon

FROM openjdk:20-jdk-oracle
EXPOSE 8080:8080
RUN mkdir /app
COPY --from=build /home/gradle/src/build/libs/*.jar /app/dolores-server.jar
ENTRYPOINT ["java","-jar","/app/dolores-server.jar"]
