package de.minikonto.exception;

/** Request breaks a business rule, e.g. transfer to the same account. HTTP 400. */
public class InvalidRequestException extends RuntimeException {
    public InvalidRequestException(String message) {
        super(message);
    }
}
