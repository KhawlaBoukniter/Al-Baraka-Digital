FROM openjdk:17-jdk-slim
LABEL authors="boukn"

COPY target/digital-bank-0.0.1-SNAPSHOT.jar app.jar

ENV JWT_SECRET=760dd8a6f14d3ff63f266801fb40e74b69ee5bb53eae088b9c9a229620764eb3
ENV DB_URL=jdbc:postgresql://db:5432/albaraka
ENV DB_USER=postgres
ENV DB_PASSWORD=postgres

ENTRYPOINT ["java", "-jar", "app.jar"]