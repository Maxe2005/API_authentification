FROM maven:3.9.6-amazoncorretto-21 as build
RUN mkdir -p /Api_authentification
COPY src /Api_authentification/src
COPY pom.xml /Api_authentification
WORKDIR /Api_authentification
RUN mvn clean package

FROM amazoncorretto:21.0.2-alpine3.19
COPY --from=build /Api_authentification/target/*.jar /app.jar
EXPOSE 8080/tcp
ENTRYPOINT ["java", "-Dspring.profiles.active=docker", "-jar", "/app.jar"]