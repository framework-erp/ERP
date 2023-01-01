package erp.repository;

import erp.process.ProcessEntity;

import java.util.Map;
import java.util.Set;

/**
 * 对内的仓库操作集合
 */
public class InnerRepository {
    private Store store;
    private Mutexes mutexes;

    public InnerRepository(Store store, Mutexes mutexes) {
        this.store = store;
        this.mutexes = mutexes;
    }

    public void flushProcessEntities(Map<Object, Object> entitiesToInsert, Map<Object, ProcessEntity> entitiesToUpdate, Set<Object> idsToRemoveEntity) {
        store.saveAll(entitiesToInsert, entitiesToUpdate);
        store.removeAll(idsToRemoveEntity);
        mutexes.removeAll(idsToRemoveEntity);
    }

    public void releaseProcessEntity(Set<Object> ids) {
        mutexes.unlockAll(ids);
    }
}
