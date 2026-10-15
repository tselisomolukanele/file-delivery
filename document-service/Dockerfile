# Stage 1: Build the native executable
FROM quay.io/quarkus/ubi-quarkus-mandrel-builder-image:23.1-java17 AS build
COPY --chown=quarkus:quarkus mvnw /project/mvnw
COPY --chown=quarkus:quarkus .mvn /project/.mvn
COPY --chown=quarkus:quarkus pom.xml /project/
USER quarkus
WORKDIR /project
RUN ./mvnw dependency:go-offline

COPY --chown=quarkus:quarkus src /project/src
RUN ./mvnw -B -DskipTests package -Dnative -Dquarkus.native.container-build=true

# Stage 2: Create the minimal execution image
FROM registry.access.redhat.com/ubi9/openjdk-21-runtime:1.24

ENV LANGUAGE='en_US:en'

# We make four distinct layers so if there are application changes the library layers can be re-used
COPY --chown=185 target/quarkus-app/lib/ /deployments/lib/
COPY --chown=185 target/quarkus-app/*.jar /deployments/
COPY --chown=185 target/quarkus-app/app/ /deployments/app/
COPY --chown=185 target/quarkus-app/quarkus/ /deployments/quarkus/

EXPOSE 8080
USER 185
ENV JAVA_OPTS_APPEND="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"

ENTRYPOINT [ "/opt/jboss/container/java/run/run-java.sh" ]
