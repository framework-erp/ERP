package erp.repository.impl;

import java.util.concurrent.atomic.AtomicInteger;

public class MemMutex {
    private AtomicInteger lock;
    private String lockProcess;

    /**
     * 创建一个mutex，创建出来就是被创建的process锁上的
     */
    public MemMutex(String lockProcess) {
        this.lock = new AtomicInteger(1);
        this.lockProcess = lockProcess;
    }

    public boolean lock(String processName) {
        if (lock.compareAndSet(0, 1)) {
            lockProcess = processName;
            return true;
        } else {
            return false;
        }
    }

    public void unlock() {
        lock.set(0);
    }

    public String getLockProcess() {
        return lockProcess;
    }
}
