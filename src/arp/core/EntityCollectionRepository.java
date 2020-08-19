package arp.core;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class EntityCollectionRepository<ID, T> {

	private static AtomicInteger ids = new AtomicInteger();

	private int id = ids.incrementAndGet();

	private Store<ID, T> store;

	protected EntityCollectionRepository(Store<ID, T> store) {
		this.store = store;
	}

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

		T entityFromStore = store.createIfAbsent(id, entity);
		processContext.takeEntityFromRepoAndPutInProcess(this.id, id, entityFromStore);
		return entityFromStore;
	}

	protected abstract ID getId(T entity);

	void updateEntitiesInProcess(Map<ID, T> entities) {
		store.checkAndUpdateAll(entities);
	}

	void saveEntitiesInProcess(Map<ID, T> entities) {
		store.saveAll(entities);
	}

	void removeEntitiesInProcess(Map<ID, T> entities) {
		store.removeAll(entities.keySet());
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

}
