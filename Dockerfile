# Init container to setup keycloak on Kubernetes
FROM maven:3.9.6-eclipse-temurin-17
COPY . /app
WORKDIR /app
RUN mkdir -p /opt/keycloak/providers
RUN mvn clean compile package

# The "providers" directory is a volume in Kubernetes
ENTRYPOINT [ "cp" ]
CMD [ "/app/target/keycloak-dapr-events.jar" , "/opt/keycloak/providers/keycloak-dapr-events.jar" ]
