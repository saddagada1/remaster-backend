#Build
FROM maven:3.9.6-amazoncorretto-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean install -DskipTests

#Run
FROM amazoncorretto:21-alpine
WORKDIR /app
COPY --from=build /app/target/remaster-0.0.1-SNAPSHOT.jar ./remaster.jar
EXPOSE 8080
CMD ["java", "-jar", "remaster.jar"]