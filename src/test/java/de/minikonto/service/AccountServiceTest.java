package de.minikonto.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import de.minikonto.exception.AccountNotFoundException;
import de.minikonto.exception.InsufficientFundsException;
import de.minikonto.exception.InvalidAmountException;
import de.minikonto.exception.InvalidRequestException;
import de.minikonto.model.Account;
import de.minikonto.model.Transfer;
import de.minikonto.repository.TransferRepository;

/** Service tests against the real H2 SQL database. Each test is rolled back afterwards. */
@SpringBootTest
@Transactional
class AccountServiceTest {

    @Autowired
    private AccountService service;

    @Autowired
    private TransferRepository transfers;

    private static BigDecimal eur(String value) {
        return new BigDecimal(value);
    }

    @Test
    @DisplayName("Open account with initial deposit")
    void openAccountWithDeposit() {
        Account account = service.openAccount("Anna", eur("100.00"));
        assertThat(account.getId()).isNotNull();
        assertThat(service.getAccount(account.getId()).getBalance()).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("Open account without initial deposit starts at 0.00")
    void openAccountWithoutDeposit() {
        Account account = service.openAccount("Anna", null);
        assertThat(account.getBalance()).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("Negative initial deposit is rejected")
    void negativeInitialDeposit() {
        assertThatThrownBy(() -> service.openAccount("Anna", eur("-5")))
                .isInstanceOf(InvalidAmountException.class);
    }

    @Test
    @DisplayName("Transfer moves money and is recorded")
    void transferMovesMoney() {
        Account anna = service.openAccount("Anna", eur("100.00"));
        Account ben = service.openAccount("Ben", eur("20.00"));

        Transfer transfer = service.transfer(anna.getId(), ben.getId(), eur("30.00"));

        assertThat(service.getAccount(anna.getId()).getBalance()).isEqualByComparingTo("70.00");
        assertThat(service.getAccount(ben.getId()).getBalance()).isEqualByComparingTo("50.00");
        assertThat(transfers.findById(transfer.getId())).isPresent();
    }

    @Test
    @DisplayName("Transfer with insufficient funds changes nothing")
    void transferInsufficientFunds() {
        Account anna = service.openAccount("Anna", eur("10.00"));
        Account ben = service.openAccount("Ben", eur("0"));
        long transfersBefore = transfers.count();

        assertThatThrownBy(() -> service.transfer(anna.getId(), ben.getId(), eur("10.01")))
                .isInstanceOf(InsufficientFundsException.class);

        assertThat(anna.getBalance()).isEqualByComparingTo("10.00");
        assertThat(ben.getBalance()).isEqualByComparingTo("0.00");
        assertThat(transfers.count()).isEqualTo(transfersBefore);
    }

    @Test
    @DisplayName("Transfer to the same account is rejected")
    void transferToSameAccount() {
        Account anna = service.openAccount("Anna", eur("100.00"));
        assertThatThrownBy(() -> service.transfer(anna.getId(), anna.getId(), eur("1.00")))
                .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    @DisplayName("Transfer to an unknown account is rejected, sender keeps the money")
    void transferToUnknownAccount() {
        Account anna = service.openAccount("Anna", eur("100.00"));
        assertThatThrownBy(() -> service.transfer(anna.getId(), 999_999L, eur("1.00")))
                .isInstanceOf(AccountNotFoundException.class);
        assertThat(anna.getBalance()).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("History shows the transfer for sender and receiver")
    void historyForBothSides() {
        Account anna = service.openAccount("Anna", eur("100.00"));
        Account ben = service.openAccount("Ben", eur("0"));
        Account carl = service.openAccount("Carl", eur("0"));
        service.transfer(anna.getId(), ben.getId(), eur("5.00"));

        assertThat(service.history(anna.getId())).hasSize(1);
        assertThat(service.history(ben.getId())).hasSize(1);
        assertThat(service.history(carl.getId())).isEmpty();
    }
}
