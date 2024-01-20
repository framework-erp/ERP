package erp.repository.impl.mem;

import erp.repository.Repository;

import java.util.ArrayList;
import java.util.List;

public class MemRepository<E, ID> extends Repository<E, ID> {
    protected MemRepository() {
        this.store = new MemStore<>();
        this.mutexes = new MemMutexes<>();
    }

    public MemRepository(Class<E> entityType) {
        super(entityType.getName());
        this.store = new MemStore<>();
        this.mutexes = new MemMutexes<>();
    }

    public List<ID> queryAllIds() {
        return new ArrayList<>(((MemStore) store).getIdSet());
    }
}
