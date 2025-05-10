FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY common/pom.xml common/
COPY hotel-service/pom.xml hotel-service/
COPY reservation-service/pom.xml reservation-service/
COPY notification-service/pom.xml notification-service/
COPY api-gateway/pom.xml api-gateway/
RUN mvn -B dependency:go-offline -pl reservation-service -am

COPY common/ common/
COPY reservation-service/ reservation-service/
RUN mvn -B package -DskipTests -pl reservation-service -am

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/reservation-service/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
