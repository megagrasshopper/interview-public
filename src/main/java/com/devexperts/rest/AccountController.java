package com.devexperts.rest;

import com.devexperts.account.Account;
import com.devexperts.account.AccountDto;
import com.devexperts.account.Transfer;
import com.devexperts.service.AccountService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Api("account operations")
public class AccountController extends AbstractAccountController {

    private final AccountService accountService;
    private final ConversionService conversionService;

    @ApiOperation("Create account")
    @PostMapping("/account")
    public void createAccount(@Valid @RequestBody AccountDto account) {
        accountService.createAccount(conversionService.convert(account, Account.class));
    }

    @ApiOperation("Get account info by id")
    @GetMapping("/account/{id}")
    public AccountDto getAccount(@PathVariable("id") long id) {
        return conversionService.convert(accountService.getAccount(id), AccountDto.class);
    }

    @ApiOperation("Clear all accounts")
    @DeleteMapping("/account")
    public void clearAccounts() {
        accountService.clear();
    }


    @Override
    public ResponseEntity<Void> transfer(long sourceId, long targetId, double amount) {
        return transferMoney(new Transfer()
                .setAmount(amount)
                .setSourceId(sourceId)
                .setTargetId(targetId));
    }

    @ApiOperation("Transfer money from one account to another")
    @PostMapping(value = "/transfer")
    public ResponseEntity<Void> transferMoney(@Valid @RequestBody Transfer transfer) {

        accountService.transfer(transfer.getSourceId(), transfer.getTargetId(), transfer.getAmount());
        return ResponseEntity.ok().build();
    }
}