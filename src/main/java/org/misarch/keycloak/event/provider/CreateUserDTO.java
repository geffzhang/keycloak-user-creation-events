package org.misarch.keycloak.event.provider;

/**
 * Event for creating a user.
 *
 * @param username the username of the user
 * @param firstName the first name of the user
 * @param lastName the last name of the user
 * @param id the UUID of the user
 */
public record CreateUserDTO(String username, String firstName, String lastName, String id) {}
