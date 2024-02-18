package org.misarch.keycloak.event.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.jboss.logging.Logger;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerTransaction;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.KeycloakModelUtils;

/**
 * Event listener provider for publishing events to Dapr.
 */
public class DaprEventListenerProvider implements EventListenerProvider {

    /**
     * Logger for log messages.
     */
    private static final Logger log = Logger.getLogger(DaprEventListenerProvider.class);

    /**
     * Keycloak event listener transaction.
     */
    private final EventListenerTransaction tx =
            new EventListenerTransaction(this::publishAdminEvent, this::publishEvent);

    /**
     * Keycloak session for data access.
     */
    private final KeycloakSession session;

    /**
     * Creates a new DaprEventListenerProvider instance.
     *
     * @param session Keycloak session
     */
    public DaprEventListenerProvider(KeycloakSession session) {
        session.getTransactionManager().enlistAfterCompletion(tx);
        this.session = session;
    }

    @Override
    public void close() {}

    @Override
    public void onEvent(Event event) {
        tx.addEvent(event.clone());
    }

    @Override
    public void onEvent(AdminEvent adminEvent, boolean includeRepresentation) {
        tx.addAdminEvent(adminEvent, includeRepresentation);
    }

    /**
     * Potentially publishes an event to Dapr.
     *
     * @param event Event to potentially publish
     */
    private void publishEvent(final Event event) {
        if (EventType.REGISTER.equals(event.getType())) {
            handleUserRegister(event.getRealmId(), event.getUserId());
        }
    }

    /**
     * Potentially publishes an admin event to Dapr.
     *
     * @param adminEvent Admin event to potentially publish
     * @param includeRepresentation when false, event listener should NOT include representation field in the resulting action
     */
    private void publishAdminEvent(final AdminEvent adminEvent, final boolean includeRepresentation) {
        if (ResourceType.USER.equals(adminEvent.getResourceType())
                && OperationType.CREATE.equals(adminEvent.getOperationType())) {
            handleUserRegister(
                    adminEvent.getRealmId(), adminEvent.getResourcePath().substring("users/".length()));
        }
    }

    /**
     * Handles user registration.
     *
     * @param realmId id of the realm where the user was registered
     * @param userId id of the registered user
     */
    private void handleUserRegister(final String realmId, final String userId) {
        log.infof("User %s in realm %s was registered", userId, realmId);
        KeycloakModelUtils.runJobInTransaction(session.getKeycloakSessionFactory(), session -> {
            final RealmModel realm = session.realms().getRealm(realmId);
            final UserModel user = session.users().getUserById(realm, userId);
            log.infof(
                    "Registered user [name=\"%s\", email=\"%s\", first name=\"%s\", last name=\"%s\"]",
                    user.getUsername(), user.getEmail(), user.getFirstName(), user.getLastName());
            try {
                final ObjectMapper objectMapper = new ObjectMapper();
                final CreateUserDTO event =
                        new CreateUserDTO(user.getUsername(), user.getFirstName(), user.getLastName(), user.getId());
                final HttpClient httpClient = HttpClient.newHttpClient();
                final HttpRequest.BodyPublisher bodyPublisher;
                bodyPublisher = HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(event));
                final HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:3500/v1.0/publish/pubsub/user/user/create"))
                        .header("Content-Type", "application/json")
                        .POST(bodyPublisher)
                        .build();
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (IOException | InterruptedException e) {
                log.error("Failed sending dapr request", e);
                throw new RuntimeException(e);
            }
        });
    }
}
