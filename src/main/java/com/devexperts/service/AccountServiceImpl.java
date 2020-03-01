package com.devexperts.service;

import com.devexperts.account.Account;
import com.devexperts.account.AccountKey;
import com.devexperts.exception.AccountNotFoundException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AccountServiceImpl implements AccountService {

    private static final Map<AccountKey, Account> accounts = new ConcurrentHashMap<>();

    @Override
    public void clear() {
        accounts.clear();
    }

    @Override
    public void createAccount(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("Account should not be null");
        }
        accounts.put(account.getAccountKey(), account);
    }

    @Override
    public Account getAccount(long id) {
        Account account = accounts.get(AccountKey.valueOf(id));
        return account == null ? null : account.clone();
    }

    @Override
    public void transfer(Account source, Account target, double amount) {
        if (source == null) {
            throw new AccountNotFoundException("source should not be null");
        }
        if (target == null) {
            throw new AccountNotFoundException("target should not be null");
        }
        transfer(source.getAccountId(), target.getAccountId(), amount);
    }

    @Override
    public void transfer(Long sourceId, Long targetId, double amount) {

        Account source = accounts.get(AccountKey.valueOf(sourceId));
        if (source == null) {
            throw new AccountNotFoundException("account with id " + sourceId + " is not found");
        }
        Account target = accounts.get(AccountKey.valueOf(targetId));
        if (target == null) {
            throw new AccountNotFoundException("account with id " + targetId + " is not found");
        }
        if (source.equals(target)) {
            throw new IllegalArgumentException("source and target should not be equal");
        }

        boolean withdrawCompleted = false;
        Lock firstLock;
        Lock secondLock;

        //to avoid deadlock. First lock is acquired to account with minimal id
        if (sourceId < targetId) {
            firstLock = source.getWriteLock();
            secondLock = target.getWriteLock();
        } else {
            firstLock = target.getWriteLock();
            secondLock = source.getWriteLock();
        }
        firstLock.lock();
        secondLock.lock();

        try {
            source.withdraw(amount);
            withdrawCompleted = true;
            target.deposit(amount);
        } catch (RuntimeException ex) {
            log.error(ex.toString(), ex);

            // transaction-like behaviour. Completed withdraw and exception means broken deposit. We should return
            // money to source
            if (withdrawCompleted) {
                source.deposit(amount);
            }

            throw ex;
        } finally {
            secondLock.unlock();
            firstLock.unlock();
        }

    }
}
