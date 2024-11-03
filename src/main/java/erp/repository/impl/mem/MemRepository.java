package erp.repository.impl.mem;

import erp.AppContext;
import erp.repository.Repository;

import java.util.ArrayList;
import java.util.List;

public class MemRepository<E, ID> extends Repository<E, ID> {
    protected MemRepository() {
        this.store = new MemStore<>();
        this.mutexes = new MemMutexes<>();
        AppContext.registerRepository(this);
    }

    public MemRepository(Class<E> entityType) {
        super(entityType);
        this.store = new MemStore<>();
        this.mutexes = new MemMutexes<>();
        AppContext.registerRepository(this);
    }

    public MemRepository(Class<E> entityType, String repositoryName) {
        super(entityType, repositoryName);
        this.store = new MemStore<>();
        this.mutexes = new MemMutexes<>();
        AppContext.registerRepository(this);
    }

    public List<ID> queryAllIds() {
        return new ArrayList<>(((MemStore) store).getIdSet());
    }
}
