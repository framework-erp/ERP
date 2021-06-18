package arp.repository;

import java.util.HashMap;
import java.util.Map;

import arp.process.CreatedProcessEntityState;
import arp.process.ProcessEntity;
import arp.process.RemovedProcessEntityState;
import arp.process.TakenProcessEntityState;
import arp.repository.copy.EntityCopier;

public class RepositoryProcessEntities<I, E> {

	private int repositoryId;

	private Map<I, ProcessEntity<E>> entities = new HashMap<>();

	public RepositoryProcessEntities(int repositoryId) {
		this.repositoryId = repositoryId;
	}

	public void takeEntityFromRepoAndPutInProcess(I entityId, E entity) {
		ProcessEntity<E> processEntity = new ProcessEntity<>();
		processEntity.setInitialEntitySnapshot(EntityCopier.copy(entity));
		processEntity.setEntity(entity);
		processEntity.setState(new TakenProcessEntityState());
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
		processEntity.updateStateByTake();
		return processEntity;
	}

	public E copyEntity(I entityId) {
		ProcessEntity<E> processEntity = entities.get(entityId);
		if (processEntity == null) {
			return null;
		}
		return EntityCopier.copy(processEntity.getEntity());
	}

	public ProcessEntity<E> findEntity(I entityId) {
		return entities.get(entityId);
	}

	public void putEntityInProcess(I entityId, E entity) {
		ProcessEntity<E> processEntity = entities.get(entityId);
		if (processEntity == null) {
			processEntity = new ProcessEntity<>();
			processEntity.setEntity(entity);
			processEntity.setState(new CreatedProcessEntityState());
			entities.put(entityId, processEntity);
		} else {
			processEntity.updateStateByPut();
		}
	}

	public int getRepositoryId() {
		return repositoryId;
	}

	public Map<I, ProcessEntity<E>> getEntities() {
		return entities;
	}

}
