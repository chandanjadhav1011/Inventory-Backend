FROM eclipse-temurin:21-jre

WORKDIR /app

COPY target/Inventory-Backend-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8443

ENTRYPOINT ["java", "-jar", "app.jar"]
