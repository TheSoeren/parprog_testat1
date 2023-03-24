package com.task3;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class UpgradeableReadWriteLock {
    private final Lock lock = new ReentrantLock();
    private final Condition writeCondition = lock.newCondition();
    private final Condition upgradeCondition = lock.newCondition();
    private final Condition readCondition = lock.newCondition();
    private int readers = 0;
    private boolean writer = false;
    private Thread upgradableThread = null;
    private boolean isUpgraded = false;

    public void readLock() throws InterruptedException {
        lock.lock();
        try {
            while (writer) {
                readCondition.await();
            }

            readers++;
        } finally {
            lock.unlock();
        }
    }

    public void readUnlock() {
        lock.lock();
        try {
            readers--;

            if (readers == 0) {
                writeCondition.signal();
            }
        } finally {
            lock.unlock();
        }
    }

    public void writeLock() throws InterruptedException {
        lock.lock();
        try {
            while (writer || readers > 0 || !upgradeableLockAvailable() || (hasUpgradeLock() && isUpgraded) ) {
                writeCondition.await();
            }

            if (hasUpgradeLock()) {
                isUpgraded = true;
            }

            writer = true;
        } finally {
            lock.unlock();
        }
    }

    public void writeUnlock() {
        lock.lock();
        try {
            writer = false;

            if (isUpgraded) {
                isUpgraded = false;
            }

            readCondition.signalAll();
            upgradeCondition.signal();
            writeCondition.signal();
        } finally {
            lock.unlock();
        }
    }

    public void upgradeableReadLock() throws InterruptedException {
        lock.lock();
        try {
            while (writer || !upgradeableLockAvailable()) {
                upgradeCondition.await();
            }

            upgradableThread = Thread.currentThread();
        } finally {
            lock.unlock();
        }
    }

    public void upgradeableReadUnlock() {
        lock.lock();
        try {
            upgradableThread = null;

            readCondition.signalAll();
            upgradeCondition.signal();
            writeCondition.signal();
        } finally {
            lock.unlock();
        }
    }

    private boolean hasUpgradeLock() {
        if (upgradableThread == null) return false;

        return upgradableThread.equals(Thread.currentThread());
    }

    private boolean upgradeableLockAvailable() {
        return hasUpgradeLock() || upgradableThread == null;
    }
}
