package arp.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class Repository<ID, T> {

	private static AtomicInteger ids = new AtomicInteger();

	private static Repository[] repositories = new Repository[1024];

	private int id;

	private Map<ID, T> mockStore;

	private boolean mock = false;

	static Repository getRepository(int id) {
		return repositories[id];
	}

	protected Repository() {
		id = ids.incrementAndGet();
		repositories[id] = this;
	}

	protected Repository(boolean mock) {
		this();
		this.mock = mock;
		if (mock) {
			mockStore = new HashMap<>();
		}
	}

	protected abstract ID getId(T entity);

	public T findByIdForUpdate(ID id) {
		ProcessContext processContext = ThreadBoundProcessContextArray.getProcessContext();
		if (!processContext.isStarted()) {
			throw new RuntimeException("can not use repository without a process");
		}

		ProcessEntity<T> processEntity = processContext.getEntityInProcessForTake(this.id, id);
		if (processEntity != null) {
			ProcessEntityState entityState = processEntity.getState();
			if (entityState instanceof CreatedProcessEntityState || entityState instanceof TakenProcessEntityState) {
				return processEntity.getEntity();
			} else {
				return null;
			}
		}

		T entity = doFindByIdForUpdate(id);
		if (entity != null) {
			processContext.takeEntityFromRepoAndPutInProcess(this.id, id, entity);
		}
		return entity;
	}

	private T doFindByIdForUpdate(ID id) {
		if (!mock) {
			return findByIdForUpdateFromStore(id);
		} else {
			return mockStore.get(id);
		}
	}

	protected abstract T findByIdForUpdateFromStore(ID id);

	public T findById(ID id) {
		ProcessContext processContext = ThreadBoundProcessContextArray.getProcessContext();
		if (!processContext.isStarted()) {
			throw new RuntimeException("can not use repository without a process");
		}
		return doFindById(id);
	}

	private T doFindById(ID id) {
		if (!mock) {
			return findByIdFromStore(id);
		} else {
			return mockStore.get(id);
		}
	}

	protected abstract T findByIdFromStore(ID id);

	public void save(T entity) {

		ProcessContext processContext = ThreadBoundProcessContextArray.getProcessContext();
		if (!processContext.isStarted()) {
			throw new RuntimeException("can not use repository without a process");
		}

		ID id = getId(entity);
		processContext.putEntityInProcess(this.id, id, entity);

	}

	public T saveIfAbsent(T entity) {
		ProcessContext processContext = ThreadBoundProcessContextArray.getProcessContext();
		if (!processContext.isStarted()) {
			throw new RuntimeException("can not use repository without a process");
		}

		ID id = getId(entity);

		ProcessEntity<T> processEntity = processContext.putIfAbsentEntityInProcess(this.id, id, entity);
		if (processEntity != null) {
			ProcessEntityState entityState = processEntity.getState();
			if (!(entityState instanceof TransientProcessEntityState)) {
				return processEntity.getEntity();
			}
		}

		T entityFromStore = doSaveIfAbsent(id, entity);
		processContext.takeEntityFromRepoAndPutInProcess(this.id, id, entityFromStore);
		return entityFromStore;
	}

	private T doSaveIfAbsent(ID id, T entity) {
		if (!mock) {
			return saveIfAbsentToStore(id, entity);
		} else {
			T t = mockStore.putIfAbsent(id, entity);
			return t == null ? entity : t;
		}
	}

	protected abstract T saveIfAbsentToStore(ID id, T entity);

	public T remove(ID id) {
		ProcessContext processContext = ThreadBoundProcessContextArray.getProcessContext();
		if (!processContext.isStarted()) {
			throw new RuntimeException("can not use repository without a process");
		}
		ProcessEntity<T> processEntity = processContext.removeEntityInProcess(this.id, id);
		if (processEntity != null) {
			return processEntity.getEntity();
		}

		T entityFromStore = doFindByIdForUpdate(id);
		if (entityFromStore != null) {
			processContext.takeEntityFromRepoAndPutInProcessAsRemoved(this.id, id, entityFromStore);
		}
		return entityFromStore;

	}

	void deleteEntities(Set<ID> ids) {

		if (!mock) {
			removeAllToStore(ids);
		} else {
			for (ID id : ids) {
				mockStore.remove(id);
			}
		}

	}

	protected abstract void removeAllToStore(Set<ID> ids);

	void updateEntities(Map<ID, T> entitiesToReturn) {

		if (!mock) {
			updateAllToStore(entitiesToReturn);
		} else {
		}

	}

	protected abstract void updateAllToStore(Map<ID, T> entities);

	void createEntities(Map<ID, T> entitiesToCreate) {

		if (!mock) {
			saveAllToStore(entitiesToCreate);
		} else {
			mockStore.putAll(entitiesToCreate);
		}

	}

	protected abstract void saveAllToStore(Map<ID, T> entities);

	void returnEntities(Set<ID> ids) {
		if (!mock) {
			unlockAllToStore(ids);
		} else {
		}
	}

	protected abstract void unlockAllToStore(Set<ID> ids);
}
