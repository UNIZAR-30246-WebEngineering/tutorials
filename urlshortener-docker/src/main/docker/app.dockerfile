FROM openjdk:alpine
ADD build/libs/urlshortener-docker-0.1.0.jar app.jar
ENTRYPOINT ["java","-jar", "-Dspring.profiles.active=docker", "/app.jar"]
