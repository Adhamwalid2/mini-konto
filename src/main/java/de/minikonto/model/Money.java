package de.minikonto.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

import de.minikonto.exception.InvalidAmountException;

/**
 * Central rules for money amounts: required, positive, at most 2 decimal places.
 */
public final class Money {

    private Money() {
    }

    public static BigDecimal requireValidAmount(BigDecimal amount) {
        if (amount == null) {
            throw new InvalidAmountException("Amount is required.");
        }
        if (amount.signum() <= 0) {
            throw new InvalidAmountException("Amount must be greater than 0.");
        }
        if (amount.stripTrailingZeros().scale() > 2) {
            throw new InvalidAmountException("Amount must have at most 2 decimal places.");
        }
        return amount.setScale(2, RoundingMode.UNNECESSARY);
    }
}
