package arp.core;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ProcessContext {

	private boolean started;

	private Map<Integer, RepositoryProcessEntities<?, ?>> processEntities = new HashMap<>();

	public void startProcess() {
		if (started) {
			throw new RuntimeException("can not start a process in another started process");
		}
		started = true;
	}

	public void finishProcess() {
		try {
			flushProcessEntities();
		} catch (Exception e) {
			releaseAcquiredLocks();
			clear();
			started = false;
			throw new RuntimeException("save and update process entities faild", e);
		}
		releaseAcquiredLocks();
		clear();
		started = false;
	}

	private void flushProcessEntities() throws Exception {
		for (RepositoryProcessEntities entities : processEntities.values()) {
			EntityCollectionRepository repository = entityCollectionRepositories.get(entities.getRepositoryId());
			repository.removeEntitiesInProcess(entities.getRemoveEntities());
			repository.saveEntitiesInProcess(entities.getPutEntities());
			repository.updateEntitiesInProcess(entities.getGetEntities());
		}
	}

	public boolean isStarted() {
		return started;
	}

	public <ID, T> ProcessEntity<T> getEntityInProcessForTake(int repositoryId, ID entityId) {
		RepositoryProcessEntities entities = processEntities.get(repositoryId);
		if (entities == null) {
			return null;
		}
		return entities.takeEntity(entityId);
	}

	public <ID, T> void takeEntityFromRepoAndPutInProcess(int repositoryId, ID entityId, T entity) {
		RepositoryProcessEntities entities = processEntities.get(repositoryId);
		if (entities == null) {
			entities = new RepositoryProcessEntities<>(repositoryId);
			processEntities.put(repositoryId, entities);
		}
		entities.takeEntityFromRepoAndPutInProcess(entityId, entity);
	}

	public <ID, T> void takeEntityFromRepoAndPutInProcessAsRemoved(int repositoryId, ID entityId, T entity) {
		RepositoryProcessEntities entities = processEntities.get(repositoryId);
		if (entities == null) {
			entities = new RepositoryProcessEntities<>(repositoryId);
			processEntities.put(repositoryId, entities);
		}
		entities.takeEntityFromRepoAndPutInProcessAsRemoved(entityId, entity);
	}

	public <ID, T> void putEntityInProcess(int repositoryId, ID entityId, T entity) {
		RepositoryProcessEntities entities = processEntities.get(repositoryId);
		if (entities == null) {
			entities = new RepositoryProcessEntities<>(repositoryId);
			processEntities.put(repositoryId, entities);
		}
		entities.putEntityInProcess(entityId, entity);
	}

	public <ID, T> ProcessEntity<T> putIfAbsentEntityInProcess(int repositoryId, ID entityId, T entity) {
		RepositoryProcessEntities entities = processEntities.get(repositoryId);
		if (entities == null) {
			return null;
		}
		ProcessEntity<T> processEntity = entities.findEntity(entityId);
		if (processEntity == null) {
			return null;
		}
		if (processEntity.getState() instanceof RemovedProcessEntityState) {
			processEntity.setEntity(entity);
			processEntity.updateStateByPut();
			return processEntity;
		} else if (processEntity.getState() instanceof TransientProcessEntityState) {
			return null;
		} else {
			return processEntity;
		}
	}

	public <ID, T> ProcessEntity<T> removeEntityInProcess(int repositoryId, ID entityId) {
		RepositoryProcessEntities entities = processEntities.get(repositoryId);
		if (entities == null) {
			return null;
		}
		ProcessEntity<T> processEntity = entities.findEntity(entityId);
		if (processEntity == null) {
			return null;
		}
		if (processEntity.getState() instanceof TransientProcessEntityState) {
			return null;
		}
		processEntity.updateStateByRemove();
		return processEntity;
	}

	public <ID, T> void addEntityCollectionRepository(int repositoryId, EntityCollectionRepository<ID, T> repository) {
		entityCollectionRepositories.putIfAbsent(repositoryId, repository);
	}

	public void addAcquiredLock(AtomicInteger lock) {
		acquiredLocks.add(lock);
	}

	public void processFaild() {
		releaseAcquiredLocks();
		clear();
		started = false;
	}

	private void clear() {
		processEntities.clear();
		entityCollectionRepositories.clear();
		acquiredLocks.clear();
	}

	private void releaseAcquiredLocks() {
		for (AtomicInteger lock : acquiredLocks) {
			lock.set(0);
		}
	}

	public <ID> void removeCollectionEntityInProcess(int repositoryId, ID entityId) {
		RepositoryProcessEntities entities = processEntities.get(repositoryId);
		if (entities == null) {
			entities = new RepositoryProcessEntities<>(repositoryId);
			processEntities.put(repositoryId, entities);
		}
		entities.removeEntity(entityId);
	}

	public <ID, T> void putCollectionEntityInProcessForRemove(int repositoryId, ID entityId, T entity) {
		RepositoryProcessEntities entities = processEntities.get(repositoryId);
		if (entities == null) {
			entities = new RepositoryProcessEntities<>(repositoryId);
			processEntities.put(repositoryId, entities);
		}
		entities.putEntityForRemove(entityId, entity);
	}

}
