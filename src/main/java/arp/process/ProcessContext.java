package arp.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import arp.repository.Repository;
import arp.repository.RepositoryProcessEntities;
import arp.util.Unsafe;

public class ProcessContext {

	private boolean started;

	private Map<Integer, RepositoryProcessEntities<?, ?>> processEntities = new HashMap<>();

	private List<AtomicInteger> singleEntityAcquiredLocks = new ArrayList<>();

	private List<Object> arguments = new ArrayList<>();

	private Object result;

	private boolean dontPublishWhenResultIsNull;

	private String processDesc;

	private boolean publish;

	public void startProcess() {
		if (started) {
			throw new RuntimeException(
					"can not start a process in another started process");
		}
		started = true;
		Unsafe.loadFence();
	}

	public void finishProcess() {
		Unsafe.storeFence();
		try {
			flushProcessEntities();
		} catch (Exception e) {
			try {
				releaseAcquiredLocks();
			} catch (Exception e1) {
			}
			clear();
			started = false;
			throw new RuntimeException("flush process entities faild", e);
		}
		try {
			releaseAcquiredLocks();
		} catch (Exception e) {
		}
		clear();
		started = false;
	}

	private void flushProcessEntities() throws Exception {
		for (RepositoryProcessEntities entities : processEntities.values()) {
			Repository repository = Repository.getRepository(entities
					.getRepositoryId());
			Map processEntities = entities.getEntities();
			Map entitiesToCreate = new HashMap();
			Map entitiesToUpdate = new HashMap();
			Set idsToRemove = new HashSet();

			for (Object obj : processEntities.entrySet()) {
				Entry entry = (Entry) obj;
				Object id = entry.getKey();
				ProcessEntity processEntity = (ProcessEntity) entry.getValue();
				if (processEntity.getState() instanceof CreatedProcessEntityState) {
					entitiesToCreate.put(id, processEntity.getEntity());
				} else if (processEntity.getState() instanceof TakenProcessEntityState) {
					if (processEntity.changed()) {
						entitiesToUpdate.put(id, processEntity.getEntity());
					}
				} else if (processEntity.getState() instanceof RemovedProcessEntityState) {
					idsToRemove.add(id);
				}
			}
			if (!idsToRemove.isEmpty()) {
				repository.deleteEntities(idsToRemove);
			}
			if (!entitiesToUpdate.isEmpty()) {
				repository.updateEntities(entitiesToUpdate);
			}
			if (!entitiesToCreate.isEmpty()) {
				repository.createEntities(entitiesToCreate);
			}
		}
	}

	public boolean isStarted() {
		return started;
	}

	public <I, E> ProcessEntity<E> getEntityInProcessForTake(int repositoryId,
			I entityId) {
		RepositoryProcessEntities<I, E> entities = (RepositoryProcessEntities<I, E>) processEntities
				.get(repositoryId);
		if (entities == null) {
			return null;
		}
		return entities.takeEntity(entityId);
	}

	public <I, E> E copyEntityInProcess(int repositoryId, I entityId) {
		RepositoryProcessEntities<I, E> entities = (RepositoryProcessEntities<I, E>) processEntities
				.get(repositoryId);
		if (entities == null) {
			return null;
		}
		return entities.copyEntity(entityId);
	}

	public <I, E> void takeEntityFromRepoAndPutInProcess(int repositoryId,
			I entityId, E entity) {
		RepositoryProcessEntities<I, E> entities = (RepositoryProcessEntities<I, E>) processEntities
				.get(repositoryId);
		if (entities == null) {
			entities = new RepositoryProcessEntities<>(repositoryId);
			processEntities.put(repositoryId, entities);
		}
		entities.takeEntityFromRepoAndPutInProcess(entityId, entity);
	}

	public <I, E> void takeEntityFromRepoAndPutInProcessAsRemoved(
			int repositoryId, I entityId, E entity) {
		RepositoryProcessEntities<I, E> entities = (RepositoryProcessEntities<I, E>) processEntities
				.get(repositoryId);
		if (entities == null) {
			entities = new RepositoryProcessEntities<>(repositoryId);
			processEntities.put(repositoryId, entities);
		}
		entities.takeEntityFromRepoAndPutInProcessAsRemoved(entityId, entity);
	}

	public <I, E> void putEntityInProcess(int repositoryId, I entityId, E entity) {
		RepositoryProcessEntities<I, E> entities = (RepositoryProcessEntities<I, E>) processEntities
				.get(repositoryId);
		if (entities == null) {
			entities = new RepositoryProcessEntities<>(repositoryId);
			processEntities.put(repositoryId, entities);
		}
		entities.putEntityInProcess(entityId, entity);
	}

	public <I, E> ProcessEntity<E> putIfAbsentEntityInProcess(int repositoryId,
			I entityId, E entity) {
		RepositoryProcessEntities<I, E> entities = (RepositoryProcessEntities<I, E>) processEntities
				.get(repositoryId);
		if (entities == null) {
			return null;
		}
		ProcessEntity<E> processEntity = entities.findEntity(entityId);
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

	public <I, E> ProcessEntity<E> removeEntityInProcess(int repositoryId,
			I entityId) {
		RepositoryProcessEntities<I, E> entities = (RepositoryProcessEntities<I, E>) processEntities
				.get(repositoryId);
		if (entities == null) {
			return null;
		}
		ProcessEntity<E> processEntity = entities.findEntity(entityId);
		if (processEntity == null) {
			return null;
		}
		if (processEntity.getState() instanceof TransientProcessEntityState) {
			return null;
		}
		processEntity.updateStateByRemove();
		return processEntity;
	}

	public void processFaild() {
		try {
			releaseAcquiredLocks();
		} catch (Exception e) {
		}
		clear();
		started = false;
	}

	private void clear() {
		processEntities.clear();
		arguments.clear();
		result = null;
	}

	private void releaseAcquiredLocks() throws Exception {
		for (RepositoryProcessEntities entities : processEntities.values()) {
			Repository repository = Repository.getRepository(entities
					.getRepositoryId());

			Map processEntities = entities.getEntities();
			Set idsToUnlock = new HashSet();

			for (Object obj : processEntities.entrySet()) {
				Entry entry = (Entry) obj;
				Object id = entry.getKey();
				ProcessEntity processEntity = (ProcessEntity) entry.getValue();
				if (processEntity.getState() instanceof TakenProcessEntityState) {
					idsToUnlock.add(id);
				} else if (processEntity.getState() instanceof RemovedProcessEntityState) {
					idsToUnlock.add(id);
				}
			}

			repository.returnEntities(idsToUnlock);

		}
		for (AtomicInteger lock : singleEntityAcquiredLocks) {
			lock.set(0);
		}
	}

	public void addSingleEntityAcquiredLock(AtomicInteger lock) {
		singleEntityAcquiredLocks.add(lock);
	}

	public void recordProcessResult(Object result) {
		this.result = result;
	}

	public void setDontPublishWhenResultIsNull(
			boolean dontPublishWhenResultIsNull) {
		this.dontPublishWhenResultIsNull = dontPublishWhenResultIsNull;
	}

	public void recordProcessDesc(String clsName, String mthName,
			String processName) {
		if (!processName.trim().isEmpty()) {
			processDesc = processName;
		} else {
			processDesc = clsName + "." + mthName;
		}
	}

	public Object getResult() {
		return result;
	}

	public boolean isDontPublishWhenResultIsNull() {
		return dontPublishWhenResultIsNull;
	}

	public String getProcessDesc() {
		return processDesc;
	}

	public boolean isPublish() {
		return publish;
	}

	public void setPublish(boolean publish) {
		this.publish = publish;
	}

	public void recordProcessArgument(Object argument) {
		arguments.add(argument);
	}

	public List<Object> getArguments() {
		return arguments;
	}

}
