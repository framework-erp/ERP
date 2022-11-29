package arp.repository;

import java.util.HashMap;
import java.util.Map;

import arp.process.CreatedProcessEntityState;
import arp.process.ProcessEntity;
import arp.process.RemovedProcessEntityState;
import arp.process.TakenProcessEntityState;
import arp.process.states.CreatedInProcState;
import arp.process.states.TakenFromRepoState;
import arp.repository.copy.EntityCopier;

public class RepositoryProcessEntities<I, E> {

    private int repositoryId;

    private Map<I, ProcessEntity<E>> entities = new HashMap<>();

    public RepositoryProcessEntities(int repositoryId) {
        this.repositoryId = repositoryId;
    }

    public void addEntityTaken(I entityId, E entity) {
        ProcessEntity<E> processEntity = new ProcessEntity<>();
        processEntity.setInitialEntitySnapshot(EntityCopier.copy(entity));
        processEntity.setEntity(entity);
        processEntity.setState(new TakenFromRepoState());
        entities.put(entityId, processEntity);
    }

    public void takeEntityFromRepoAndPutInProcessAsRemoved(I entityId, E entity) {
        ProcessEntity<E> processEntity = new ProcessEntity<>();
        processEntity.setInitialEntitySnapshot(EntityCopier.copy(entity));
        processEntity.setEntity(entity);
        processEntity.setState(new RemovedProcessEntityState());
        entities.put(entityId, processEntity);
    }

    public ProcessEntity<E> takeEntity(I entityId) {
        ProcessEntity<E> processEntity = entities.get(entityId);
        if (processEntity == null) {
            return null;
        }
        if (processEntity.isAvailable()) {
            processEntity.changeStateByTake();
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
            entities.put(entityId, processEntity);
        }
        processEntity.setEntity(entity);
        if (!processEntity.isAvailable()) {
            processEntity.changeStateByPut();
        }
    }

    public void removeEntity(I entityId) {
        ProcessEntity<E> processEntity = entities.get(entityId);
        if (processEntity == null) {
            return ;
        }
        processEntity.changeStateByRemove();
    }

    public int getRepositoryId() {
        return repositoryId;
    }

    public Map<I, ProcessEntity<E>> getEntities() {
        return entities;
    }


}
