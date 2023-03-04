package erp.repository.impl.dto;

import erp.process.ProcessEntity;
import erp.repository.Store;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class DTOProxyStore<E, DTO, ID> implements Store<E, ID> {

    private Store<DTO, ID> dtoStore;

    public DTOProxyStore(Store<DTO, ID> dtoStore) {
        this.dtoStore = dtoStore;
    }

    @Override
    public E load(ID id) {
        DTO dto = dtoStore.load(id);
        if (dto == null) {
            return null;
        }
        return toEntity(dto);
    }

    protected abstract E toEntity(DTO load);

    @Override
    public void insert(ID id, E entity) {
        DTO dto = null;
        if (entity != null) {
            dto = toDTO(entity);
        }
        dtoStore.insert(id, dto);
    }

    protected abstract DTO toDTO(E entity);

    @Override
    public void saveAll(Map<Object, Object> entitiesToInsert, Map<Object, ProcessEntity> entitiesToUpdate) {
        Map<Object, Object> dtoToInsert = new HashMap<>(entitiesToInsert.size());
        Map<Object, ProcessEntity> dtoToUpdate = new HashMap<>(entitiesToUpdate.size());
        for (Map.Entry<Object, Object> entry : entitiesToInsert.entrySet()) {
            dtoToInsert.put(entry.getKey(), toDTO((E) entry.getValue()));
        }
        for (Map.Entry<Object, ProcessEntity> entry : entitiesToUpdate.entrySet()) {
            ProcessEntity processDto = new ProcessEntity();
            processDto.setEntity(toDTO((E) entry.getValue().getEntity()));
            processDto.setInitialEntitySnapshot(toDTO((E) entry.getValue().getInitialEntitySnapshot()));
            processDto.setState(entry.getValue().getState());
            dtoToUpdate.put(entry.getKey(), processDto);
        }
        dtoStore.saveAll(dtoToInsert, dtoToUpdate);
    }

    @Override
    public void removeAll(Set<Object> ids) {
        dtoStore.removeAll(ids);
    }
}
