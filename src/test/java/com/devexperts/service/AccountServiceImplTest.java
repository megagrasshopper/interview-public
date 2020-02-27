package com.devexperts.service;

import static com.devexperts.util.AccountUtil.account;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.devexperts.account.Account;
import com.devexperts.account.AccountKey;
import com.devexperts.exception.AccountNotFoundException;
import com.devexperts.exception.InsufficientFundsException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
class AccountServiceImplTest {

    private final AccountService accountService = new AccountServiceImpl();

    @BeforeEach
    void setUp() {
        accountService.clear();
    }

    @Test
    void clear() {
        long id = 1L;
        accountService.createAccount(account(id));
        accountService.clear();
        assertNull(accountService.getAccount(id));
    }

    @Test
    void createAccount() {
        long id = 2L;
        assertNull(accountService.getAccount(id));

        Account account = account(id);
        accountService.createAccount(account);
        Account received = accountService.getAccount(id);
        assertNotNull(received);
        assertEquals(account, received);

        assertThrows(IllegalArgumentException.class, () -> accountService.createAccount(null));
    }


    @Test
    void getAccount() {
        long id = 3L;
        Account account = account(id);
        accountService.createAccount(account);
        Account received = accountService.getAccount(id);
        assertNotNull(received);
        assertEquals(account, received);
        assertNotSame(account, received);
    }


    @Test
    void transfer() {
        long sourceId = 1L;
        long targetId = 2L;
        accountService.createAccount(account(sourceId, 10.));
        accountService.createAccount(account(targetId, 5.));

        accountService.transfer(accountService.getAccount(sourceId), accountService.getAccount(targetId), 5.);
        Account source = accountService.getAccount(sourceId);
        Account target = accountService.getAccount(targetId);

        assertEquals(5., source.getBalance());
        assertEquals(10., target.getBalance());
    }

    @Test
    void transferNullAccount() {
        long id = 1L;

        accountService.createAccount(account(id, 10.));

        assertThrows(AccountNotFoundException.class, () ->
                accountService.transfer(accountService.getAccount(id), null, 5.));

        assertThrows(AccountNotFoundException.class, () ->
                accountService.transfer(null, accountService.getAccount(id), 5.));
    }

    @Test
    void transfer1() {
        long sourceId = 1L;
        long targetId = 2L;
        accountService.createAccount(account(sourceId, 10.));
        accountService.createAccount(account(targetId, 5.));

        accountService.transfer(sourceId, targetId, 4.);
        Account source = accountService.getAccount(sourceId);
        Account target = accountService.getAccount(targetId);

        assertEquals(6., source.getBalance());
        assertEquals(9., target.getBalance());
    }

    @Test
    void transferAccountNotFound() {
        long id = 1L;

        accountService.createAccount(account(id, 10.));

        assertThrows(AccountNotFoundException.class, () ->
                accountService.transfer(2L, id, 5.));

        assertThrows(AccountNotFoundException.class, () ->
                accountService.transfer(id, 3L, 5.));

        assertThrows(IllegalArgumentException.class, () ->
                accountService.transfer(id, id, 5.));
    }

    @Test
    void transferWithdrawException() {
        long sourceId = 3L;
        long targetId = 2L;
        accountService.createAccount(account(sourceId, 10.));
        accountService.createAccount(account(targetId, 5.));

        assertThrows(InsufficientFundsException.class, () ->
                accountService.transfer(sourceId, targetId, 15.));

        Account source = accountService.getAccount(sourceId);
        Account target = accountService.getAccount(targetId);
        assertEquals(10., source.getBalance());
        assertEquals(5., target.getBalance());
    }

    @Test
    void transferDepositException() {
        long sourceId = 1L;
        long targetId = 2L;

        Lock writeLock = new ReentrantReadWriteLock().writeLock();

        Account mockAccount = mock(Account.class);
        when(mockAccount.getAccountKey()).thenReturn(AccountKey.valueOf(targetId));
        when(mockAccount.getAccountId()).thenReturn(targetId);
        when(mockAccount.getWriteLock()).thenReturn(writeLock);
        doThrow(new RuntimeException()).when(mockAccount).deposit(any(Double.class));

        accountService.createAccount(account(sourceId, 10.));
        accountService.createAccount(mockAccount);

        assertThrows(RuntimeException.class, () ->
                accountService.transfer(sourceId, targetId, 5));

        Account source = accountService.getAccount(sourceId);
        assertEquals(10., source.getBalance());
    }

    @Test
    void multiThreadTransferTest() throws ExecutionException, InterruptedException {

        long id1 = 3L;
        long id2 = 2L;
        accountService.createAccount(account(id1, 10000.));
        accountService.createAccount(account(id2, 10000.));

        ExecutorService executor = Executors.newFixedThreadPool(50);

        CompletableFuture.allOf(IntStream.rangeClosed(1, 10000).parallel()
                .mapToObj(i -> {
                            long sourceId;
                            long targetId;
                            if (i % 2 == 0) {
                                sourceId = id1;
                                targetId = id2;
                            } else {
                                sourceId = id2;
                                targetId = id1;
                            }
                            return CompletableFuture.runAsync(
                                    () -> accountService.transfer(sourceId, targetId, 1.), executor);
                        }
                ).toArray(CompletableFuture[]::new)
        ).get();

        executor.shutdown();

        Account a1 = accountService.getAccount(id1);
        Account a2 = accountService.getAccount(id2);

        assertEquals(10000., a1.getBalance());
        assertEquals(10000., a2.getBalance());
    }
}