package arp.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ProcessContext {

	private boolean started;

	private Map<Integer, RepositoryProcessEntities<?, ?>> processEntities = new HashMap<>();

	private Map<Integer, EntityCollectionRepository<?, ?>> entityCollectionRepositories = new HashMap<>();

	private List<AtomicInteger> acquiredLocks = new ArrayList<>();

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

	public <ID, T> T findCollectionEntityInProcess(int repositoryId, ID entityId) {
		RepositoryProcessEntities entities = processEntities.get(repositoryId);
		if (entities == null) {
			return null;
		}
		return (T) entities.findEntity(entityId);
	}

	public <ID, T> T findCollectionEntityInProcessForRead(int repositoryId, ID entityId) {
		RepositoryProcessEntities entities = processEntities.get(repositoryId);
		if (entities == null) {
			return null;
		}
		return (T) entities.findEntityForRead(entityId);
	}

	public <ID, T> void putCollectionEntityInProcessForGet(int repositoryId, ID entityId, T entity) {
		RepositoryProcessEntities entities = processEntities.get(repositoryId);
		if (entities == null) {
			entities = new RepositoryProcessEntities<>(repositoryId);
			processEntities.put(repositoryId, entities);
		}
		entities.putEntityForGet(entityId, entity);
	}

	public <ID, T> void putCollectionEntityInProcessForGetForRead(int repositoryId, ID entityId, T entity) {
		RepositoryProcessEntities entities = processEntities.get(repositoryId);
		if (entities == null) {
			entities = new RepositoryProcessEntities<>(repositoryId);
			processEntities.put(repositoryId, entities);
		}
		entities.putEntityForGetForRead(entityId, entity);
	}

	public <ID, T> void putCollectionEntityInProcessForPut(int repositoryId, ID entityId, T entity) {
		RepositoryProcessEntities entities = processEntities.get(repositoryId);
		if (entities == null) {
			entities = new RepositoryProcessEntities<>(repositoryId);
			processEntities.put(repositoryId, entities);
		}
		entities.putEntityForPut(entityId, entity);
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

	public <ID> boolean collectionEntityRemovedInProcess(int repositoryId, ID entityId) {
		RepositoryProcessEntities entities = processEntities.get(repositoryId);
		if (entities == null) {
			entities = new RepositoryProcessEntities<>(repositoryId);
			processEntities.put(repositoryId, entities);
		}
		return entities.hasRemovedEntity(entityId);
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
