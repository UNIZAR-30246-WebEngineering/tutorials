# URL Shortener in Docker
[Docker](https://www.docker.com/) is an open platform for developers and sysadmins to build, 
ship, and run distributed applications, whether on laptops, data center VMs, or the cloud.

This tutorial shows:
- How to define your app’s environment with a *Dockerfile* so it can be reproduced anywhere.
- Define the services that make up your app in `docker-compose.yml` so they can be run together in an isolated environment.
- How to adapt your *Spring Boot* app to the new environment.
- How to run `docker-compose` to start and run your entire app.

This tutorial uses the final code of [Create a URL Shortener step-by-step](../urlshortener/README.md). 
The objective is the creation of an application with two containers as the diagram below.

![Docker app](img/docker-app.png)

## The *Dockerfile* 
Create a file in `src/main/docker` named `app.dockerfile`:
```dockerfile 
FROM openjdk:alpine
ADD build/libs/urlshortener-docker-0.1.0.jar app.jar
ENTRYPOINT ["java","-jar", "-Dspring.profiles.active=docker", "/app.jar"]
```

This file tells *Docker* to build an image using as base [a minimal Docker image based on Alpine Linux
with OpenJKD 8 installed](https://hub.docker.com/_/openjdk/). Then add to the image the jar 
`urlshortener-docker-0.1.0.jar`. Finally, it configures the image to run such `jar` file with the profile
`docker` active.

## The *docker-compose.yml* 
Create a file named `docker-compose.yml`:
```yml
 version : '2'
 services:
 
   app:
     build:
       context: .
       dockerfile: src/main/docker/app.dockerfile
     ports:
       - "8080:8080"
     links:
       - db
 
   db:
     image: redis:latest
```
This file defines two services (`app` and `db`). The first will
use the instructions in `app.dockerfile` for being built. It will expose
its port 8080 to the external world as 8080 and will have access to the `db`
container through network internal to *Docker*. The second (`db`) will 
be [the latest redis image](https://hub.docker.com/_/redis/). The hostname of this
container in the internal network will be `db`.
 
## Changes from the original code
The application needs to know where is the Redis instance within docker. 
An approach is the use of a properties file. `app.dockerfile` tells
Docker to run the app with the profile `docker` enabled. Then we should create
the file `application-docker.properties` in `src/main/resources` with the following
content:
```
spring.redis.host=db
```
As simply as this.

## Running the app with Docker compose
We assume that Docker is correctly installed in the machine. To ease its use
we add the following task to `build.gradle`:
```groovy
tsk compose(type: Exec) {
    dependsOn   'build'
    commandLine 'docker-compose', 'build'
    errorOutput = new ByteArrayOutputStream()
    standardOutput = new ByteArrayOutputStream()
}

task up(type: Exec) {
    dependsOn   'compose'
    commandLine 'docker-compose', 'up', '--remove-orphans'
}

```
This adds two new tasks. `gradle compose` builds the containers. `gradle up` runs them.
And that's it. Just do `gradle up` and your dockerised app will start to work.
