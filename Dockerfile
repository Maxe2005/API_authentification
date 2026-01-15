FROM maven:3.9.6-amazoncorretto-21 as build
RUN mkdir -p /demo2026
COPY src /demo2026/src
COPY pom.xml /demo2026
WORKDIR /demo2026
RUN mvn clean package

FROM amazoncorretto:21.0.2-alpine3.19
COPY --from=build /demo2026/target/*.jar /app.jar
EXPOSE 8080/tcp
ENTRYPOINT ["java", "-Dspring.profiles.active=docker", "-jar", "/app.jar"]
