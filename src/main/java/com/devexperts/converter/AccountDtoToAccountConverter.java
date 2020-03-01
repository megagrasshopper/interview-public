package com.devexperts.converter;

import com.devexperts.account.Account;
import com.devexperts.account.AccountDto;
import org.springframework.core.convert.converter.Converter;

public class AccountDtoToAccountConverter implements Converter<AccountDto, Account> {
    @Override
    public Account convert(AccountDto source) {
        if (source == null) {
            return null;
        }
        return Account.valueOf(source.getId(), source.getFirstName(), source.getLastName(), source.getBalance());
    }
}
