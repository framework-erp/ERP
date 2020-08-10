package arp.core;

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
}
