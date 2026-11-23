package de.minikonto.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import de.minikonto.model.Transfer;

public interface TransferRepository extends JpaRepository<Transfer, Long> {

    /** All transfers where the account is sender or receiver, newest first. */
    List<Transfer> findByFromAccountIdOrToAccountIdOrderByCreatedAtDescIdDesc(Long fromAccountId, Long toAccountId);
}
