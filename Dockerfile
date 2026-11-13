FROM eclipse-temurin:21-jre
COPY target/eventsProject-1.0.0.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]