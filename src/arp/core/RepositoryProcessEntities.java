package arp.core;

import java.util.HashMap;
import java.util.Map;

public class RepositoryProcessEntities<ID, T> {

	private int repositoryId;

	private Map<ID, ProcessEntity<T>> entities = new HashMap<>();

	public RepositoryProcessEntities(int repositoryId) {
		this.repositoryId = repositoryId;
	}

	public void takeEntityFromRepoAndPutInProcess(ID entityId, T entity) {
		ProcessEntity<T> processEntity = new ProcessEntity<>();
		processEntity.setEntity(entity);
		processEntity.setState(new TakenProcessEntityState());
		entities.put(entityId, processEntity);
	}

	public ProcessEntity<T> takeEntity(ID entityId) {
		ProcessEntity<T> processEntity = entities.get(entityId);
		if (processEntity == null) {
			return null;
		}
		processEntity.updateStateByTake();
		return processEntity;
	}

	public void putEntityInProcess(ID entityId, T entity) {
		ProcessEntity<T> processEntity = entities.get(entityId);
		if (processEntity == null) {
			processEntity = new ProcessEntity<>();
			processEntity.setEntity(entity);
			processEntity.setState(new CreatedProcessEntityState());
			entities.put(entityId, processEntity);
		} else {
			processEntity.updateStateByPut();
		}
	}

}
