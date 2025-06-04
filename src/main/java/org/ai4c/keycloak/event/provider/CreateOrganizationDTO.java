package org.ai4c.keycloak.event.provider;

/**
 * Event for creating a organization.
 *
 * @param name the name of the organization
 * @param id the UUID of the organization 
 */
public record CreateOrganizationDTO(String name, String id) {}