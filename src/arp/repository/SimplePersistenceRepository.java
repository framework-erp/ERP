package arp.repository;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public abstract class SimplePersistenceRepository<ID, T> extends PersistenceRepository<ID, T> {

	@Override
	protected T findByIdForUpdateImpl(ID id) {
		return findByIdImpl(id);
	}

	@Override
	protected T saveIfAbsentImpl(ID id, T entity) {
		T existsEntity = findByIdImpl(id);
		if (existsEntity == null) {
			saveImpl(id, entity);
		}
		return existsEntity;
	}

	@Override
	protected void updateAndUnlockBatchImpl(Map<ID, T> entitiesToUpdate) {
		for (Entry<ID, T> entry : entitiesToUpdate.entrySet()) {
			updateImpl(entry.getKey(), entry.getValue());
		}
	}

	protected abstract void updateImpl(ID id, T entity);

	@Override
	protected void updateAndUnlockImpl(ID id, T entity) {
		updateImpl(id, entity);
	}

	@Override
	protected void saveBatchImpl(Map<ID, T> entities) {
		for (Entry<ID, T> entry : entities.entrySet()) {
			saveImpl(entry.getKey(), entry.getValue());
		}
	}

	@Override
	protected void removeBatchImpl(Set<ID> ids) {
		for (ID id : ids) {
			removeImpl(id);
		}
	}

	@Override
	protected void unlockBatchImpl(Set<ID> ids) {
	}

	@Override
	protected void unlockImpl(ID id) {
	}

}
