package arp.repository;

import java.util.HashMap;
import java.util.Map;

import arp.process.ProcessEntity;
import arp.process.states.CreatedInProcState;
import arp.process.states.TakenFromRepoState;
import arp.repository.copy.EntityCopier;

public class RepositoryProcessEntities<I, E> {

    private String aggType;

    private Map<I, ProcessEntity<E>> entities = new HashMap<>();

    public RepositoryProcessEntities(String aggType) {
        this.aggType = aggType;
    }

    public void addEntityTaken(I entityId, E entity) {
        ProcessEntity<E> processEntity = new ProcessEntity<>();
        processEntity.setInitialEntitySnapshot(EntityCopier.copy(entity));
        processEntity.setEntity(entity);
        processEntity.setState(new TakenFromRepoState());
        entities.put(entityId, processEntity);
    }

    public ProcessEntity<E> takeProcessEntity(I entityId) {
        ProcessEntity<E> processEntity = entities.get(entityId);
        if (processEntity == null) {
            return null;
        }
        processEntity.changeStateByTake();
        return processEntity;
    }

    public ProcessEntity<E> getProcessEntity(I entityId) {
        ProcessEntity<E> processEntity = entities.get(entityId);
        if (processEntity == null) {
            return null;
        }
        return processEntity;
    }

    public E copyEntity(I entityId) {
        return EntityCopier.copy(getEntity(entityId));
    }

    public E getEntity(I entityId) {
        ProcessEntity<E> processEntity = entities.get(entityId);
        if (processEntity == null) {
            return null;
        }
        return processEntity.getEntity();
    }

    public ProcessEntity<E> findEntity(I entityId) {
        return entities.get(entityId);
    }

    public void addNewEntity(I entityId, E entity) {
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

    public void removeEntity(I entityId) {
        ProcessEntity<E> processEntity = entities.get(entityId);
        if (processEntity == null) {
            return;
        }
        processEntity.changeStateByRemove();
    }

    public String getAggType() {
        return aggType;
    }

    public Map<I, ProcessEntity<E>> getEntities() {
        return entities;
    }


}
