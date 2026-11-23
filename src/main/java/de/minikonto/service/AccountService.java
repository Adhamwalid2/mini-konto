package de.minikonto.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.minikonto.exception.AccountNotFoundException;
import de.minikonto.exception.InvalidRequestException;
import de.minikonto.model.Account;
import de.minikonto.model.Money;
import de.minikonto.model.Transfer;
import de.minikonto.repository.AccountRepository;
import de.minikonto.repository.TransferRepository;

/**
 * Use cases of the bank. Every public method runs in one database transaction:
 * either everything is saved, or nothing is.
 */
@Service
@Transactional
public class AccountService {

    private final AccountRepository accounts;
    private final TransferRepository transfers;

    public AccountService(AccountRepository accounts, TransferRepository transfers) {
        this.accounts = accounts;
        this.transfers = transfers;
    }

    public Account openAccount(String owner, BigDecimal initialDeposit) {
        Account account = new Account(owner);
        if (initialDeposit != null && initialDeposit.signum() != 0) {
            account.deposit(initialDeposit);
        }
        return accounts.save(account);
    }

    @Transactional(readOnly = true)
    public List<Account> listAccounts() {
        return accounts.findAll(org.springframework.data.domain.Sort.by("id"));
    }

    @Transactional(readOnly = true)
    public Account getAccount(Long id) {
        return accounts.findById(id).orElseThrow(() -> new AccountNotFoundException(id));
    }

    public Account deposit(Long id, BigDecimal amount) {
        Account account = getAccount(id);
        account.deposit(amount);
        return account;
    }

    public Account withdraw(Long id, BigDecimal amount) {
        Account account = getAccount(id);
        account.withdraw(amount);
        return account;
    }

    public Transfer transfer(Long fromId, Long toId, BigDecimal amount) {
        if (fromId == null || toId == null) {
            throw new InvalidRequestException("Sender and receiver account are required.");
        }
        if (fromId.equals(toId)) {
            throw new InvalidRequestException("Cannot transfer to the same account.");
        }
        BigDecimal valid = Money.requireValidAmount(amount);
        Account from = getAccount(fromId);
        Account to = getAccount(toId);

        from.withdraw(valid);
        to.deposit(valid);

        return transfers.save(new Transfer(fromId, toId, valid));
    }

    @Transactional(readOnly = true)
    public List<Transfer> history(Long accountId) {
        getAccount(accountId); // 404 if the account does not exist
        return transfers.findByFromAccountIdOrToAccountIdOrderByCreatedAtDescIdDesc(accountId, accountId);
    }
}
