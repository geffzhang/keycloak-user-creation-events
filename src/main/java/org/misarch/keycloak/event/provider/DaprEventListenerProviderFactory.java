package org.misarch.keycloak.event.provider;

import org.jboss.logging.Logger;
import org.keycloak.Config.Scope;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

/**
 * Factory for creating DaprEventListenerProvider instances.
 */
public class DaprEventListenerProviderFactory implements EventListenerProviderFactory {

    /**
     * Logger for log messages.
     */
    private static final Logger log = Logger.getLogger(DaprEventListenerProviderFactory.class);

    @Override
    public EventListenerProvider create(KeycloakSession session) {
        return new DaprEventListenerProvider(session);
    }

    @Override
    public void init(Scope config) {
        log.info("Initializing DaprEventListenerProviderFactory");
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {}

    @Override
    public void close() {}

    @Override
    public String getId() {
        return "keycloak-dapr-events";
    }
}
