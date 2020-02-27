package com.devexperts.account;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Unique Account identifier
 *
 * <p>
 * NOTE: we suspect that later {@link #accountId} is not going to be uniquely identifying an account,
 * as we might add human-readable account representation and some clearing codes for partners.
 */
@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
public class AccountKey {
    private final long accountId;

    public static AccountKey valueOf(long accountId) {
        return new AccountKey(accountId);
    }
}
