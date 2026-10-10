package org.example.backend.common.exception;

/**
 * Action tentée par un utilisateur qui n'en a pas le droit métier
 * (ex: un artisan tente d'accepter la demande destinée à un autre).
 * Mappée en HTTP 403 Forbidden.
 */
public class UnauthorizedActionException extends RuntimeException {
    public UnauthorizedActionException(String message) {
        super(message);
    }
}
