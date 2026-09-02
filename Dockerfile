# Dockerfile
# Builds the TOP Racing WAR and runs it on Eclipse GlassFish for Cloud Run.
FROM maven:3.9.11-eclipse-temurin-17 AS build

WORKDIR /workspace
COPY pom.xml .
RUN mvn -B -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -B -DskipTests package

FROM ghcr.io/eclipse-ee4j/glassfish:7.0.25

ENV PORT=8080

COPY --from=build --chown=glassfish:glassfish /workspace/target/topracingwebapp.war custom/topracingwebapp.war
COPY --chown=glassfish:glassfish docker/cloud-run-init.sh custom/init.sh

RUN sed -i 's/\r$//' custom/init.sh \
    && chmod +x custom/init.sh

EXPOSE 8080
CMD ["startserv"]
