package de.minikonto.exception;

/** No account with this id exists. HTTP 404. */
public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(Long id) {
        super("Account " + id + " not found.");
    }
}
