package org.example.backend.common.exception;

/**
 * Erreur métier (ex : transition de statut invalide, email déjà utilisé).
 * Mappée en HTTP 409 Conflict par le GlobalExceptionHandler.
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}