package de.minikonto.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import de.minikonto.service.AccountService;
import de.minikonto.web.Dtos.TransferRequest;
import de.minikonto.web.Dtos.TransferResponse;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {

    private final AccountService service;

    public TransferController(AccountService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransferResponse transfer(@RequestBody TransferRequest request) {
        return TransferResponse.of(
                service.transfer(request.fromAccountId(), request.toAccountId(), request.amount()));
    }
}
