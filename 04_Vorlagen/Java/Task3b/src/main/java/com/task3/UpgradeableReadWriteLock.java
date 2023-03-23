package com.task3;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class UpgradeableReadWriteLock {
    private int readCount = 0;
    private int writeCount = 0;
    private int upgradeableReadCount = 0;
    private Lock lock = new ReentrantLock();
    private Condition readCondition = lock.newCondition();
    private Condition writeCondition = lock.newCondition();
    private Condition upgradeableReadCondition = lock.newCondition();

    public void readLock() throws InterruptedException {
        lock.lock();
        try {
            while (writeCount > 0) {
                readCondition.await();
            }
            readCount++;
        } finally {
            lock.unlock();
        }
    }

    public void readUnlock() {
        lock.lock();
        try {
            readCount--;
            if (readCount == 0) {
                writeCondition.signal();
            }
        } finally {
            lock.unlock();
        }
    }

    public void upgradeableReadLock() throws InterruptedException {
        lock.lock();
        try {
            while (writeCount > 0 || upgradeableReadCount > 0) {
                upgradeableReadCondition.await();
            }
            upgradeableReadCount++;
            while (readCount > 0) {
                readCondition.await();
            }
        } finally {
            lock.unlock();
        }
    }

    public void upgradeableReadUnlock() {
        lock.lock();
        try {
            upgradeableReadCount--;
            if (upgradeableReadCount == 0) {
                readCondition.signalAll();
            }
        } finally {
            lock.unlock();
        }
    }

    public void writeLock() throws InterruptedException {
        lock.lock();
        try {
            while (writeCount > 0 || readCount > 0 || upgradeableReadCount > 0) {
                writeCondition.await();
            }
            writeCount++;
        } finally {
            lock.unlock();
        }
    }

    public void writeUnlock() {
        lock.lock();
        try {
            writeCount--;
            upgradeableReadCondition.signalAll();
            writeCondition.signal();
            readCondition.signalAll();
        } finally {
            lock.unlock();
        }
    }
}
