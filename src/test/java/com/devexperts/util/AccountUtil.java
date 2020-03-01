package com.devexperts.util;

import com.devexperts.account.Account;

public class AccountUtil {

    public static final String FIRST_NAME = "fn";
    public static final String LAST_NAME = "ln";


    public static Account account(long id) {
        return Account.valueOf(id, FIRST_NAME, LAST_NAME, 4.);
    }

    public static Account account(long id, Double balance) {
        return Account.valueOf(id, FIRST_NAME, LAST_NAME, balance);
    }
}
