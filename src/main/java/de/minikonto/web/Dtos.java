package de.minikonto.web;

import java.math.BigDecimal;
import java.time.Instant;

import de.minikonto.model.Account;
import de.minikonto.model.Transfer;
import jakarta.validation.constraints.NotBlank;

/**
 * Request and response formats of the REST API.
 * Money is returned as a string ("150.00") so no precision is lost.
 */
public final class Dtos {

    private Dtos() {
    }

    public record OpenAccountRequest(@NotBlank String owner, BigDecimal initialDeposit) {
    }

    public record AmountRequest(BigDecimal amount) {
    }

    public record TransferRequest(Long fromAccountId, Long toAccountId, BigDecimal amount) {
    }

    public record AccountResponse(Long id, String owner, String balance) {
        static AccountResponse of(Account a) {
            return new AccountResponse(a.getId(), a.getOwner(), a.getBalance().toPlainString());
        }
    }

    public record TransferResponse(Long id, Long fromAccountId, Long toAccountId, String amount, Instant createdAt) {
        static TransferResponse of(Transfer t) {
            return new TransferResponse(t.getId(), t.getFromAccountId(), t.getToAccountId(),
                    t.getAmount().toPlainString(), t.getCreatedAt());
        }
    }
}
