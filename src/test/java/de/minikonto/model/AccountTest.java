package de.minikonto.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.minikonto.exception.InsufficientFundsException;
import de.minikonto.exception.InvalidAmountException;
import de.minikonto.exception.InvalidRequestException;

/** Unit tests: business rules of an account, no database, no Spring. */
class AccountTest {

    private static BigDecimal eur(String value) {
        return new BigDecimal(value);
    }

    @Test
    @DisplayName("New account starts with balance 0.00")
    void newAccountStartsAtZero() {
        Account account = new Account("Anna");
        assertThat(account.getBalance()).isEqualByComparingTo("0.00");
        assertThat(account.getOwner()).isEqualTo("Anna");
    }

    @Test
    @DisplayName("Owner name is required")
    void ownerIsRequired() {
        assertThatThrownBy(() -> new Account("  ")).isInstanceOf(InvalidRequestException.class);
        assertThatThrownBy(() -> new Account(null)).isInstanceOf(InvalidRequestException.class);
    }

    @Test
    @DisplayName("Deposit increases the balance")
    void depositIncreasesBalance() {
        Account account = new Account("Anna");
        account.deposit(eur("100.50"));
        assertThat(account.getBalance()).isEqualByComparingTo("100.50");
    }

    @Test
    @DisplayName("Withdrawal decreases the balance")
    void withdrawDecreasesBalance() {
        Account account = new Account("Anna");
        account.deposit(eur("100.00"));
        account.withdraw(eur("30.25"));
        assertThat(account.getBalance()).isEqualByComparingTo("69.75");
    }

    @Test
    @DisplayName("Boundary: withdrawing exactly the full balance is allowed")
    void withdrawExactBalance() {
        Account account = new Account("Anna");
        account.deposit(eur("50.00"));
        account.withdraw(eur("50.00"));
        assertThat(account.getBalance()).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("Boundary: withdrawing 1 cent more than the balance is rejected, balance unchanged")
    void noOverdraft() {
        Account account = new Account("Anna");
        account.deposit(eur("50.00"));
        assertThatThrownBy(() -> account.withdraw(eur("50.01")))
                .isInstanceOf(InsufficientFundsException.class);
        assertThat(account.getBalance()).isEqualByComparingTo("50.00");
    }

    @ParameterizedTest(name = "Amount {0} is rejected")
    @ValueSource(strings = { "0", "0.00", "-1", "-0.01", "0.001", "10.999" })
    @DisplayName("Invalid amounts (zero, negative, more than 2 decimals) are rejected")
    void invalidAmountsRejected(String amount) {
        Account account = new Account("Anna");
        assertThatThrownBy(() -> account.deposit(eur(amount))).isInstanceOf(InvalidAmountException.class);
        assertThat(account.getBalance()).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("Missing amount is rejected")
    void nullAmountRejected() {
        Account account = new Account("Anna");
        assertThatThrownBy(() -> account.deposit(null)).isInstanceOf(InvalidAmountException.class);
    }

    @Test
    @DisplayName("Trailing zeros are fine: 10.500 counts as 10.50")
    void trailingZerosAccepted() {
        Account account = new Account("Anna");
        account.deposit(eur("10.500"));
        assertThat(account.getBalance()).isEqualByComparingTo("10.50");
    }
}
