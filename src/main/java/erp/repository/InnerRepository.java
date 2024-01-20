package erp.repository;

import erp.process.ProcessEntity;

import java.util.Map;
import java.util.Set;

/**
 * 对内的仓库操作集合
 */
public class InnerRepository {
    private Repository repository;

    public InnerRepository(Repository repository) {
        this.repository = repository;
    }

    public void flushProcessEntities(Map<Object, Object> entitiesToInsert, Map<Object, ProcessEntity> entitiesToUpdate, Set<Object> idsToRemoveEntity) {
        repository.store.saveAll(entitiesToInsert, entitiesToUpdate);
        repository.store.removeAll(idsToRemoveEntity);
        repository.mutexes.removeAll(idsToRemoveEntity);
    }

    public void releaseProcessEntity(Set<Object> ids) {
        repository.mutexes.unlockAll(ids);
    }
}
