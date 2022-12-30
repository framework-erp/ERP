package arp.repository;

import arp.process.ProcessEntity;
import arp.process.states.CreatedInProcState;
import arp.process.states.TakenFromRepoState;
import arp.repository.copy.EntityCopier;

import java.util.HashMap;
import java.util.Map;

public class RepositoryProcessEntities<ID, E> {

    private String entityType;

    private Map<ID, ProcessEntity<E>> entities = new HashMap<>();

    public RepositoryProcessEntities(String entityType) {
        this.entityType = entityType;
    }

    public void addEntityTaken(ID entityId, E entity) {
        ProcessEntity<E> processEntity = new ProcessEntity<>();
        processEntity.setInitialEntitySnapshot(EntityCopier.copy(entity));
        processEntity.setEntity(entity);
        processEntity.setState(new TakenFromRepoState());
        entities.put(entityId, processEntity);
    }

    public ProcessEntity<E> takeProcessEntity(ID entityId) {
        ProcessEntity<E> processEntity = entities.get(entityId);
        if (processEntity == null) {
            return null;
        }
        processEntity.changeStateByTake();
        return processEntity;
    }

    public ProcessEntity<E> getProcessEntity(ID entityId) {
        ProcessEntity<E> processEntity = entities.get(entityId);
        if (processEntity == null) {
            return null;
        }
        return processEntity;
    }

    public E copyEntity(ID entityId) {
        return EntityCopier.copy(getEntity(entityId));
    }

    public E getEntity(ID entityId) {
        ProcessEntity<E> processEntity = entities.get(entityId);
        if (processEntity == null) {
            return null;
        }
        return processEntity.getEntity();
    }

    public ProcessEntity<E> findEntity(ID entityId) {
        return entities.get(entityId);
    }

    public void addNewEntity(ID entityId, E entity) {
        ProcessEntity<E> processEntity = entities.get(entityId);
        if (processEntity == null) {
            processEntity = new ProcessEntity<>();
            processEntity.setState(new CreatedInProcState());
            processEntity.setEntity(entity);
            entities.put(entityId, processEntity);
            return;
        }
        processEntity.setEntity(entity);
        processEntity.changeStateByPut();
    }

    public void removeEntity(ID entityId) {
        ProcessEntity<E> processEntity = entities.get(entityId);
        if (processEntity == null) {
            return;
        }
        processEntity.changeStateByRemove();
    }

    public String getEntityType() {
        return entityType;
    }

    public Map<ID, ProcessEntity<E>> getEntities() {
        return entities;
    }


}
