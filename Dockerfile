FROM eclipse-temurin:21-jre
COPY target/*.jar app.jar
<<<<<<< HEAD
ENTRYPOINT ["java","-jar","/app.jar"]
=======
ENTRYPOINT ["java","-jar","/app.jar"]
>>>>>>> 88b56b4bed2e7a13c925d41c05f05048b2606ebd
