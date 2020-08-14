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

		T entityInProcess = processContext.findCollectionEntityInProcess(this.id, id);
		if (entityInProcess != null) {
			return entityInProcess;
		}

		if (processContext.collectionEntityRemovedInProcess(this.id, id)) {
			return null;
		}

		T entity = store.findForTake(id);
		if (entity != null) {
			processContext.putCollectionEntityInProcessForGet(this.id, id, entity);
			processContext.addEntityCollectionRepository(this.id, this);
		}
		return entity;
	}

	public T copy(ID id) {
		ProcessContext processContext = ThreadBoundProcessContextArray.getProcessContext();
		if (!processContext.isStarted()) {
			throw new RuntimeException("can not use repository without a process");
		}

		T entityInProcess = processContext.findCollectionEntityInProcessForRead(this.id, id);
		if (entityInProcess != null) {
			return entityInProcess;
		}

		if (processContext.collectionEntityRemovedInProcess(this.id, id)) {
			return null;
		}

		T entity = store.findByIdForRead(id);
		if (entity != null) {
			processContext.putCollectionEntityInProcessForGetForRead(this.id, id, entity);
			processContext.addEntityCollectionRepository(this.id, this);
		}
		return entity;
	}

	public T put(T entity) {

		ProcessContext processContext = ThreadBoundProcessContextArray.getProcessContext();
		if (!processContext.isStarted()) {
			throw new RuntimeException("can not use repository without a process");
		}

		ID id = getId(entity);
		if (processContext.collectionEntityRemovedInProcess(this.id, id)) {
			T entityInProcess = processContext.findCollectionEntityInProcess(this.id, id);
			if (entityInProcess != null) {
				return entityInProcess;
			}
			processContext.putCollectionEntityInProcessForPut(this.id, id, entity);
			processContext.addEntityCollectionRepository(this.id, this);
			return entity;
		}

		acquireLock(id, processContext);

		T entityInProcess = processContext.findCollectionEntityInProcess(this.id, id);
		if (entityInProcess != null) {
			return entityInProcess;
		}

		if (store.has(id)) {
			T existsEntity = store.findByIdForUpdate(id);
			processContext.putCollectionEntityInProcessForGet(this.id, id, existsEntity);
			processContext.addEntityCollectionRepository(this.id, this);
			return existsEntity;
		}

		processContext.putCollectionEntityInProcessForPut(this.id, id, entity);
		processContext.addEntityCollectionRepository(this.id, this);
		return entity;
	}

	public T putIfAbsent(T entity) {

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
		T entityInProcess = processContext.findCollectionEntityInProcess(this.id, id);
		if (entityInProcess != null) {
			processContext.removeCollectionEntityInProcess(this.id, id);
			return entityInProcess;
		}

		if (processContext.collectionEntityRemovedInProcess(this.id, id)) {
			return null;
		}

		acquireLock(id, processContext);

		T existsEntity = store.findByIdForRemoveReturn(id);
		processContext.putCollectionEntityInProcessForRemove(this.id, id, existsEntity);
		return existsEntity;

	}

}
