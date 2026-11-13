FROM eclipse-temurin:25-jre

COPY target/musicando-*.jar /app/musicando-backend.jar

WORKDIR /app

EXPOSE 8080

CMD ["java", "-jar", "-Dspring.profiles.active=prd", "musicando-backend.jar"]