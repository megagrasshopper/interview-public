package com.devexperts.account;

import com.devexperts.exception.InsufficientFundsException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Exclude;
import lombok.Getter;

@EqualsAndHashCode
public class Account implements Cloneable {

    @Exclude
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    @Exclude
    private final Lock readLock = readWriteLock.readLock();
    @Exclude
    @Getter
    private final Lock writeLock = readWriteLock.writeLock();

    @Getter
    @NotNull
    private final AccountKey accountKey;
    @Getter
    private final String firstName;
    @Getter
    private final String lastName;
    /*
    double and float are not recommended when deal with currencies, since they always carry small rounding differences
    It should be used BigDecimal. But I can't change method signatures, so we have to use double...
     */
    @NotNull
    private Double balance;

    public Account(AccountKey accountKey, String firstName, String lastName, Double balance) {
        this.accountKey = accountKey;
        this.firstName = firstName;
        this.lastName = lastName;
        this.balance = balance;
    }

    public static Account valueOf(long id, String firstName, String lastName, Double balance) {
        return new Account(AccountKey.valueOf(id), firstName, lastName, balance);
    }

    @Override
    public Account clone() {
        readLock.lock();
        try {
            return new Account(accountKey, firstName, lastName, balance);
        } finally {
            readLock.unlock();
        }
    }

    public void deposit(Double amount) {
        writeLock.lock();
        try {
            if (amount == null || amount <= 0) {
                throw new IllegalArgumentException("Deposit amount should be positive");
            }
            balance += amount;
        } finally {
            writeLock.unlock();
        }
    }

    public void withdraw(Double amount) {
        writeLock.lock();
        try {
            if (amount == null || amount <= 0) {
                throw new IllegalArgumentException("Withdraw amount should be positive");
            }
            if (amount > balance) {
                throw new InsufficientFundsException("Withdraw amount " + amount + " is greater than balance " + balance);
            }
            balance -= amount;
        } finally {
            writeLock.unlock();
        }
    }

    public Double getBalance() {
        readLock.lock();
        try {
            return balance;
        } finally {
            readLock.unlock();
        }
    }

    public void setBalance(Double balance) {
        writeLock.lock();
        try {
            if (balance == null || balance <= 0.) {
                throw new IllegalArgumentException("Balance to set should be positive");
            }
            this.balance = balance;
        } finally {
            writeLock.lock();
        }
    }

    public Long getAccountId() {
        return accountKey.getAccountId();
    }
}
