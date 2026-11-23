package de.minikonto.exception;

/** Amount is missing, not positive, or has more than 2 decimal places. HTTP 400. */
public class InvalidAmountException extends RuntimeException {
    public InvalidAmountException(String message) {
        super(message);
    }
}
