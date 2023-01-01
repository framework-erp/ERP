package erp.repository;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 对内的独立实体仓库操作集合
 */
public class InnerSingletonRepository {
    private AtomicInteger lock;

    public InnerSingletonRepository(AtomicInteger lock) {
        this.lock = lock;
    }

    public void releaseProcessEntity() {
        lock.set(0);
    }
}
