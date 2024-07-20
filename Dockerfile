FROM maven:3.9.6 AS build

WORKDIR /t1

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src src
RUN mvn package -DskipTests

FROM openjdk:21

WORKDIR /t1

COPY --from=build /t1/target/security-1.0.0.jar .

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "security-1.0.0.jar"]