# Keycloak Dapr Events

This is a Keycloak custom SPI that automatically send events with dapr.


## Compilation

There is a `Dockerfile` in the root of the project that can be used to build a Docker image with the SPI:

```bash
docker build -t keycloak-dapr-events .
```

The compiled JAR file will be located in `/app/target/keycloak-dapr-events.jar`:

```bash
docker create --name keycloak-dapr-events keycloak-dapr-events
docker cp keycloak-dapr-events:/app/target/keycloak-dapr-events.jar dist/keycloak-dapr-events.jar
docker rm keycloak-dapr-events
```


## Deployment

Copy the JAR file to your Keycloak server's `providers/` directory.



## Configuration

1. Log in to the Keycloak Admin Console.
2. Select the Realm where you want to configure the SPI.
3. Navigate to **Realm Settings** > **Events** > **Event Listeners**.
4. Find the `keycloak-dapr-events` listener in the "Available" list and select it.

[keycloak-providers](https://github.com/ObaidDev/keycloak-providers)
[mcp-keycloak](https://github.com/akoserwal/keycloak-integrations/tree/main/mcp-keycloak)
