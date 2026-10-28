# Stage 1: Build the application using Java 21 Mandrel Builder
FROM quay.io/quarkus/ubi-quarkus-mandrel-builder-image:jdk-21 AS build
COPY --chown=quarkus:quarkus mvnw /project/mvnw
COPY --chown=quarkus:quarkus .mvn /project/.mvn
COPY --chown=quarkus:quarkus pom.xml /project/
USER quarkus
WORKDIR /project
RUN ./mvnw dependency:go-offline

COPY --chown=quarkus:quarkus src /project/src
# standard JVM package build step
RUN ./mvnw -B -DskipTests package 

# Stage 2: Create the execution image with OpenJDK 21
FROM registry.access.redhat.com/ubi9/openjdk-21-runtime:1.24

ENV LANGUAGE='en_US:en'

# Copies compiled source segments securely from Stage 1
COPY --from=build --chown=185 /project/target/quarkus-app/lib/ /deployments/lib/
COPY --from=build --chown=185 /project/target/quarkus-app/*.jar /deployments/
COPY --from=build --chown=185 /project/target/quarkus-app/app/ /deployments/app/
COPY --from=build --chown=185 /project/target/quarkus-app/quarkus/ /deployments/quarkus/

EXPOSE 80
USER 185
ENV JAVA_OPTS_APPEND="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"

ENTRYPOINT [ "/opt/jboss/container/java/run/run-java.sh" ]
