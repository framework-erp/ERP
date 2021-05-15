package arp.repository;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public abstract class SimplePersistenceRepository<E, ID> extends
		PersistenceRepository<E, ID> {

	@Override
	protected E findByIdForUpdateImpl(ID id) {
		return findByIdImpl(id);
	}

	@Override
	protected E saveIfAbsentImpl(ID id, E entity) {
		E existsEntity = findByIdImpl(id);
		if (existsEntity == null) {
			saveImpl(id, entity);
		}
		return existsEntity;
	}

	@Override
	protected void updateBatchImpl(Map<ID, E> entitiesToUpdate) {
		for (Entry<ID, E> entry : entitiesToUpdate.entrySet()) {
			updateImpl(entry.getKey(), entry.getValue());
		}
	}

	@Override
	protected void saveBatchImpl(Map<ID, E> entities) {
		for (Entry<ID, E> entry : entities.entrySet()) {
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
