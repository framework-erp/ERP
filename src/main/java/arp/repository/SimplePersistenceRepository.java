package arp.repository;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public abstract class SimplePersistenceRepository<E, I> extends
		PersistenceRepository<E, I> {

	@Override
	protected E findByIdForUpdateImpl(I id) {
		return findByIdImpl(id);
	}

	@Override
	protected E saveIfAbsentImpl(I id, E entity) {
		E existsEntity = findByIdImpl(id);
		if (existsEntity == null) {
			saveImpl(id, entity);
		}
		return existsEntity;
	}

	@Override
	protected void updateBatchImpl(Map<I, E> entitiesToUpdate) {
		for (Entry<I, E> entry : entitiesToUpdate.entrySet()) {
			updateImpl(entry.getKey(), entry.getValue());
		}
	}

	@Override
	protected void saveBatchImpl(Map<I, E> entities) {
		for (Entry<I, E> entry : entities.entrySet()) {
			saveImpl(entry.getKey(), entry.getValue());
		}
	}

	@Override
	protected void removeBatchImpl(Set<I> ids) {
		for (I id : ids) {
			removeImpl(id);
		}
	}

	@Override
	protected void unlockBatchImpl(Set<I> ids) {
	}

	@Override
	protected void unlockImpl(I id) {
	}

}
