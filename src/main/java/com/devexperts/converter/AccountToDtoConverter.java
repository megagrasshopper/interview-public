package com.devexperts.converter;

import com.devexperts.account.Account;
import com.devexperts.account.AccountDto;
import org.springframework.core.convert.converter.Converter;

public class AccountToDtoConverter implements Converter<Account, AccountDto> {

    @Override
    public AccountDto convert(Account source) {
        if (source == null) {
            return null;
        }
        return new AccountDto()
                .setBalance(source.getBalance())
                .setFirstName(source.getFirstName())
                .setLastName(source.getLastName())
                .setId(source.getAccountId());
    }
}
