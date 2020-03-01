package com.devexperts.account;

import static com.devexperts.util.AccountUtil.account;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.devexperts.exception.InsufficientFundsException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AccountTest {

    @Test
    void cloneTest() {
        long id = 1L;
        Double balance = 25.5;

        Account account = account(id, balance);
        Account clone = account.clone();
        assertEquals(account, clone);
        assertNotSame(account, clone);
    }

    @Test
    void deposit() {
        Account account = account(1L, 4.);
        account.deposit(2.);
        assertEquals(6., account.getBalance());
    }

    @Test
    @DisplayName("Test null and non-positive deposit amount")
    void depositWrongAmount() {
        Account account = account(1L, 4.);

        Throwable exception = assertThrows(IllegalArgumentException.class, () -> account.deposit(null));
        assertTrue(exception.getMessage().contains("Deposit amount should be positive"));

        assertThrows(IllegalArgumentException.class, () -> account.deposit(0.));
        assertThrows(IllegalArgumentException.class, () -> account.deposit(-10.22));
    }


    @Test
    void withdraw() {
        Account account = account(1L, 4.);
        account.withdraw(2.);
        assertEquals(2., account.getBalance());
    }

    @Test
    @DisplayName("Test null and non-positive withdraw amount")
    void withdrawWrongAmount() {
        Account account = account(1L, 4.);

        Throwable exception = assertThrows(IllegalArgumentException.class, () -> account.withdraw(null));
        assertTrue(exception.getMessage().contains("Withdraw amount should be positive"));

        assertThrows(IllegalArgumentException.class, () -> account.withdraw(0.));
        assertThrows(IllegalArgumentException.class, () -> account.withdraw(-1.));
    }

    @Test
    @DisplayName("Test insufficient funds at withdraw operation")
    void withdrawInsufficientFunds() {
        Account account = account(1L, 4.);

        Throwable exception = assertThrows(InsufficientFundsException.class, () -> account.withdraw(5.));
        assertTrue(exception.getMessage().contains("Withdraw amount 5.0 is greater than balance 4.0"));
    }

    @Test
    void getAccountId() {
        assertEquals(1L, account(1L).getAccountId());
    }

    @Test
    void setBalance() {
        Account account = account(1L, 4.);
        account.setBalance(1.);
        assertEquals(1., account.getBalance());
    }

    @Test
    @DisplayName("Test null and non-positive balance amount")
    void setWrongBalance() {
        Account account = account(1L, 4.);

        Throwable exception = assertThrows(IllegalArgumentException.class, () -> account.setBalance(null));
        assertTrue(exception.getMessage().contains("Balance to set should be positive"));

        assertThrows(IllegalArgumentException.class, () -> account.setBalance(0.));
        assertThrows(IllegalArgumentException.class, () -> account.setBalance(-1.));
    }
}