package de.minikonto.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import de.minikonto.service.AccountService;
import de.minikonto.web.Dtos.AccountResponse;
import de.minikonto.web.Dtos.AmountRequest;
import de.minikonto.web.Dtos.OpenAccountRequest;
import de.minikonto.web.Dtos.TransferResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService service;

    public AccountController(AccountService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse open(@Valid @RequestBody OpenAccountRequest request) {
        return AccountResponse.of(service.openAccount(request.owner(), request.initialDeposit()));
    }

    @GetMapping
    public List<AccountResponse> list() {
        return service.listAccounts().stream().map(AccountResponse::of).toList();
    }

    @GetMapping("/{id}")
    public AccountResponse get(@PathVariable Long id) {
        return AccountResponse.of(service.getAccount(id));
    }

    @PostMapping("/{id}/deposit")
    public AccountResponse deposit(@PathVariable Long id, @RequestBody AmountRequest request) {
        return AccountResponse.of(service.deposit(id, request.amount()));
    }

    @PostMapping("/{id}/withdraw")
    public AccountResponse withdraw(@PathVariable Long id, @RequestBody AmountRequest request) {
        return AccountResponse.of(service.withdraw(id, request.amount()));
    }

    @GetMapping("/{id}/transfers")
    public List<TransferResponse> history(@PathVariable Long id) {
        return service.history(id).stream().map(TransferResponse::of).toList();
    }
}
