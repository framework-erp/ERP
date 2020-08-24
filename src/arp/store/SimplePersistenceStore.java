package arp.store;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public abstract class SimplePersistenceStore<ID, T> extends PersistenceStore<ID, T> {

	@Override
	public T findForRead(ID id) {
		return findOne(id);
	}

	protected abstract T findOne(ID id);

	@Override
	protected T findAndLock(ID id) {
		return findOne(id);
	}

	@Override
	protected T createIfAbsentAndLock(ID id, T entity) {
		create(id, entity);
		return entity;
	}

	@Override
	protected void updateAndUnlockBatch(Map<ID, T> entitiesToUpdate) {
		for (Entry<ID, T> entry : entitiesToUpdate.entrySet()) {
			update(entry.getKey(), entry.getValue());
		}
	}

	protected abstract void update(ID id, T entity);

	@Override
	protected void updateAndUnlock(ID id, T entity) {
		update(id, entity);
	}

	@Override
	protected void createBatch(Map<ID, T> entities) {
		for (Entry<ID, T> entry : entities.entrySet()) {
			create(entry.getKey(), entry.getValue());
		}
	}

	@Override
	protected void removeBatch(Set<ID> ids) {
		for (ID id : ids) {
			remove(id);
		}
	}

}
