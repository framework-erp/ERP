package erp.repository.impl;

import erp.repository.Repository;

public class MemRepository<E, ID> extends Repository<E, ID> {
    public MemRepository() {
        super(new MemStore<>(), new MemMutexes<>());
    }
}
