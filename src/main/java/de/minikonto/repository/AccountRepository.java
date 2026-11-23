package de.minikonto.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import de.minikonto.model.Account;

public interface AccountRepository extends JpaRepository<Account, Long> {
}
