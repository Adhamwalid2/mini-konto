package de.minikonto.exception;

import java.math.BigDecimal;

/** Withdrawal or transfer is larger than the balance. HTTP 422. */
public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(Long accountId, BigDecimal balance, BigDecimal amount) {
        super("Insufficient funds on account " + accountId + ": balance " + balance.toPlainString()
                + ", requested " + amount.toPlainString() + ".");
    }
}
