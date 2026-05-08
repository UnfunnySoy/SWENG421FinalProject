package model;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * model.WarehouseLock (Concurrence - Read/Write Lock Pattern)
 * Ensures thread-safe access to warehouse inventory.
 * Multiple readers can hold the read lock simultaneously.
 * Only one writer can hold the write lock, blocking all readers.
 */
public class WarehouseLock {
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

    // Live counters exposed for the UI
    private volatile int readerCount = 0;
    private volatile int writerCount = 0;
    private volatile int waitingCount = 0;

    /** Acquire the read lock. Blocks if a writer holds the lock. */
    public void readLock() {
        waitingCount++;
        rwLock.readLock().lock();
        waitingCount = Math.max(0, waitingCount - 1);
        readerCount++;
    }

    /** Acquire the write lock. Blocks all readers and other writers. */
    public void writeLock() {
        waitingCount++;
        rwLock.writeLock().lock();
        waitingCount = Math.max(0, waitingCount - 1);
        writerCount++;
    }

    /**
     * Release whichever lock the calling thread holds.
     * Callers are responsible for calling done() once for every
     * readLock() or writeLock() they called.
     */
    public void done(boolean wasWriter) {
        if (wasWriter) {
            writerCount = Math.max(0, writerCount - 1);
            rwLock.writeLock().unlock();
        } else {
            readerCount = Math.max(0, readerCount - 1);
            rwLock.readLock().unlock();
        }
    }

    // ---- UI-facing status ----
    public int getReaderCount()  { return readerCount; }
    public int getWriterCount()  { return writerCount; }
    public int getWaitingCount() { return waitingCount; }
    public boolean isIdle()      { return readerCount == 0 && writerCount == 0 && waitingCount == 0; }
}
