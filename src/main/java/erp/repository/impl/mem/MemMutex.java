package erp.repository.impl.mem;

import erp.repository.LockResult;
import java.util.concurrent.atomic.AtomicReference;

public class MemMutex {
    private final AtomicReference<String> lock;

    /**
     * 创建一个mutex，创建出来就是被创建的process锁上的
     */
    public MemMutex(String lockProcess) {
        this.lock = new AtomicReference<>(lockProcess);
    }

    public LockResult lock(String processName) {
        if (lock.compareAndSet(null, processName)) {
            return LockResult.success();
        } else {
            return LockResult.failed(lock.get());
        }
    }

    public void unlock() {
        lock.set(null);
    }

    public String getLockProcess() {
        return lock.get();
    }
}
