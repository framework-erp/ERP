package arp.core;

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

		T entity = findByIdForUpdateFromStore(id);
		if (entity != null) {
			processContext.takeEntityFromRepoAndPutInProcess(this.id, id, entity);
		}
		return entity;
	}

	private T findByIdForUpdateFromStore(ID id) {
		if (!mock) {
			return doFindByIdForUpdate(id);
		} else {
			return mockStore.get(id);
		}
	}

	protected abstract T doFindByIdForUpdate(ID id);

	public T findById(ID id) {
		ProcessContext processContext = ThreadBoundProcessContextArray.getProcessContext();
		if (!processContext.isStarted()) {
			throw new RuntimeException("can not use repository without a process");
		}
		return findByIdFromStore(id);
	}

	private T findByIdFromStore(ID id) {
		if (!mock) {
			return doFindById(id);
		} else {
			return mockStore.get(id);
		}
	}

	protected abstract T doFindById(ID id);

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

		T entityFromStore = saveIfAbsentToStore(id, entity);
		processContext.takeEntityFromRepoAndPutInProcess(this.id, id, entityFromStore);
		return entityFromStore;
	}

	private T saveIfAbsentToStore(ID id, T entity) {
		if (!mock) {
			return doSaveIfAbsent(id, entity);
		} else {
			T t = mockStore.putIfAbsent(id, entity);
			return t == null ? entity : t;
		}
	}

	protected abstract T doSaveIfAbsent(ID id, T entity);

	public T remove(ID id) {
		ProcessContext processContext = ThreadBoundProcessContextArray.getProcessContext();
		if (!processContext.isStarted()) {
			throw new RuntimeException("can not use repository without a process");
		}
		ProcessEntity<T> processEntity = processContext.removeEntityInProcess(this.id, id);
		if (processEntity != null) {
			return processEntity.getEntity();
		}

		T entityFromStore = findByIdForUpdateFromStore(id);
		if (entityFromStore != null) {
			processContext.takeEntityFromRepoAndPutInProcessAsRemoved(this.id, id, entityFromStore);
		}
		return entityFromStore;

	}

	void deleteEntities(Set<ID> ids) {
		removeAll(ids);
	}

	protected abstract void removeAll(Set<ID> ids);

	void updateEntities(Map<ID, T> entitiesToReturn) {
		updateAll(entitiesToReturn);
	}

	protected abstract void updateAll(Map<ID, T> entities);

	void createEntities(Map<ID, T> entitiesToCreate) {
		saveAll(entitiesToCreate);
	}

	protected abstract void saveAll(Map<ID, T> entities);

	void returnEntities(Set<ID> ids) {
		unlockAll(ids);
	}

	protected abstract void unlockAll(Set<ID> ids);
}
