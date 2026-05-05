FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml .
COPY incidenthub-core/pom.xml incidenthub-core/pom.xml
COPY incidenthub-infrastructure/pom.xml incidenthub-infrastructure/pom.xml
COPY incidenthub-api/pom.xml incidenthub-api/pom.xml
COPY incidenthub-worker/pom.xml incidenthub-worker/pom.xml
COPY demo-services/demo-payment-service/pom.xml demo-services/demo-payment-service/pom.xml

RUN mvn -pl incidenthub-api -am dependency:go-offline -DskipTests

COPY incidenthub-core/src incidenthub-core/src
COPY incidenthub-infrastructure/src incidenthub-infrastructure/src
COPY incidenthub-api/src incidenthub-api/src

RUN mvn -pl incidenthub-api -am clean package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /workspace/incidenthub-api/target/incidenthub-api-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]