package erp.repository.impl.mem;

import erp.process.ProcessEntity;
import erp.repository.Store;
import erp.repository.copy.EntityCopier;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MemStore<E, ID> implements Store<E, ID> {

    private Map<ID, E> data = new ConcurrentHashMap<>();

    @Override
    public E load(ID id) {
        E entitLoad = data.get(id);
        return EntityCopier.copy(entitLoad);
    }

    @Override
    public void insert(ID id, E entity) {
        if (data.containsKey(id)) {
            throw new RuntimeException("can not 'Save' since entity already exists");
        }
        data.put(id, entity);
    }

    @Override
    public void saveAll(Map<Object, Object> entitiesToInsert, Map<Object, ProcessEntity> entitiesToUpdate) {
        for (Map.Entry<Object, Object> entry : entitiesToInsert.entrySet()) {
            if (data.containsKey(entry.getKey())) {
                throw new RuntimeException("can not 'Save' since entity already exists");
            }
            data.put((ID) entry.getKey(), (E) entry.getValue());
        }
        for (Map.Entry<Object, ProcessEntity> entry : entitiesToUpdate.entrySet()) {
            data.put((ID) entry.getKey(), (E) entry.getValue().getEntity());
        }
    }

    @Override
    public void removeAll(Set<Object> ids) {
        for (Object id : ids) {
            data.remove(id);
        }
    }

    public Set<ID> getIdSet() {
        return data.keySet();
    }
}
