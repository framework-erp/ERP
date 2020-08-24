package arp.core;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class EntityCollectionRepository<ID, T> {

	private static AtomicInteger ids = new AtomicInteger();

	private static EntityCollectionRepository[] repositories = new EntityCollectionRepository[1024];

	private int id;

	private Store<ID, T> store;

	static EntityCollectionRepository getRepository(int id) {
		return repositories[id];
	}

	protected EntityCollectionRepository(Store<ID, T> store) {
		this.store = store;
		id = ids.incrementAndGet();
		repositories[id] = this;
	}

	protected abstract ID getId(T entity);

	public T take(ID id) {
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

		T entity = store.findForTake(id);
		if (entity != null) {
			processContext.takeEntityFromRepoAndPutInProcess(this.id, id, entity);
		}
		return entity;
	}

	public T copy(ID id) {
		ProcessContext processContext = ThreadBoundProcessContextArray.getProcessContext();
		if (!processContext.isStarted()) {
			throw new RuntimeException("can not use repository without a process");
		}
		return store.findForRead(id);
	}

	public void put(T entity) {

		ProcessContext processContext = ThreadBoundProcessContextArray.getProcessContext();
		if (!processContext.isStarted()) {
			throw new RuntimeException("can not use repository without a process");
		}

		ID id = getId(entity);
		processContext.putEntityInProcess(this.id, id, entity);

	}

	public T putIfAbsent(T entity) {
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

		T entityFromStore = store.createIfAbsentAndTake(id, entity);
		processContext.takeEntityFromRepoAndPutInProcess(this.id, id, entityFromStore);
		return entityFromStore;
	}

	public T remove(ID id) {
		ProcessContext processContext = ThreadBoundProcessContextArray.getProcessContext();
		if (!processContext.isStarted()) {
			throw new RuntimeException("can not use repository without a process");
		}
		ProcessEntity<T> processEntity = processContext.removeEntityInProcess(this.id, id);
		if (processEntity != null) {
			return processEntity.getEntity();
		}

		T entityFromStore = store.findForTake(id);
		if (entityFromStore != null) {
			processContext.takeEntityFromRepoAndPutInProcessAsRemoved(this.id, id, entityFromStore);
		}
		return entityFromStore;

	}

	void deleteEntities(Set<ID> ids) {
		store.removeAll(ids);
	}

	void updateEntities(Map<ID, T> entitiesToUpdate) {
		store.checkAndUpdateAll(entitiesToUpdate);
	}

	void createEntities(Map<ID, T> entitiesToCreate) {
		store.createAll(entitiesToCreate);
	}

}
