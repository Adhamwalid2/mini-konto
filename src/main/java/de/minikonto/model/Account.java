package de.minikonto.model;

import java.math.BigDecimal;

import de.minikonto.exception.InsufficientFundsException;
import de.minikonto.exception.InvalidRequestException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

/**
 * A bank account. The business rules (no overdraft, valid amounts) live here,
 * so they can be tested without a database.
 */
@Entity
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String owner;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    /** Optimistic locking: two parallel transfers cannot overwrite each other. */
    @Version
    private Long version;

    protected Account() {
        // required by JPA
    }

    public Account(String owner) {
        if (owner == null || owner.isBlank()) {
            throw new InvalidRequestException("Owner is required.");
        }
        this.owner = owner.trim();
        this.balance = BigDecimal.ZERO.setScale(2);
    }

    public void deposit(BigDecimal amount) {
        BigDecimal valid = Money.requireValidAmount(amount);
        balance = balance.add(valid);
    }

    public void withdraw(BigDecimal amount) {
        BigDecimal valid = Money.requireValidAmount(amount);
        if (balance.compareTo(valid) < 0) {
            throw new InsufficientFundsException(id, balance, valid);
        }
        balance = balance.subtract(valid);
    }

    public Long getId() {
        return id;
    }

    public String getOwner() {
        return owner;
    }

    public BigDecimal getBalance() {
        return balance;
    }
}
