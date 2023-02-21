package erp.repository.impl;

import erp.repository.Repository;

public class MemRepository<E, ID> extends Repository<E, ID> {
    protected MemRepository() {
        super(new MemStore<>(), new MemMutexes<>());
    }

    public MemRepository(Class<E> entityType) {
        super(new MemStore<>(), new MemMutexes<>(), entityType.getName());
    }
}
