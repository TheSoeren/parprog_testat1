package com.task3;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class UpgradeableReadWriteLock {
    private int readCount = 0;
    private int writeCount = 0;
    private int upgradeableReadCount = 0;
	private int upgradedLock = 0;
    private Lock lock = new ReentrantLock(true);
    private Condition noReaders = lock.newCondition();
    private Condition noWriters = lock.newCondition();
    private Condition noUpgradableReaders = lock.newCondition();

    public void readLock() throws InterruptedException {
        lock.lock();
        try {
			while (writeCount > 0) {
				noReaders.await();
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
                noWriters.signal();
            }
        } finally {
            lock.unlock();
        }
    }

    public void upgradeableReadLock() throws InterruptedException {
        lock.lock();
        try {
            while (writeCount > 0 || upgradeableReadCount > 0) {
                noUpgradableReaders.await();
            }
            upgradeableReadCount++;
        } finally {
            lock.unlock();
        }
    }

    public void upgradeableReadUnlock() {
        lock.lock();
        try {
            upgradeableReadCount--;
            if (upgradeableReadCount == 0) {
                noUpgradableReaders.signalAll();
				noWriters.signalAll();
            }
        } finally {
            lock.unlock();
        }
    }

    public void writeLock() throws InterruptedException {
        lock.lock();
        try {
            while (writeCount > 0 || readCount > 0 || upgradedLock > 0) {
                noWriters.await();
            }

			if (upgradeableReadCount > 0) {
				upgradedLock++;
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

			if (upgradedLock > 0) {
				upgradedLock++;
			}

            noUpgradableReaders.signalAll();
            noWriters.signal();
            noReaders.signalAll();
        } finally {
            lock.unlock();
        }
    }
}
