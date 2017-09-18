FROM openjdk:alpine
ARG BUILD_VERSION
ADD build/libs/urlshortener-docker-${BUILD_VERSION}.jar app.jar
ENTRYPOINT ["java","-jar", "-Dspring.profiles.active=docker", "/app.jar"]
