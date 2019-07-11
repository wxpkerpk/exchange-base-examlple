package com.bitcola.exchange.klock.lock;

import com.bitcola.exchange.klock.model.LockInfo;

/**
 * Created by kl on 2017/12/29.
 */
public interface Lock {
    Lock setLockInfo(LockInfo lockInfo);

    boolean acquire();

    void release();
}
