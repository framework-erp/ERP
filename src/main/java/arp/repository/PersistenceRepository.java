package arp.repository;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public abstract class PersistenceRepository<E, I> extends Repository<E, I> {

	@Override
	protected E findByIdForUpdateFromStore(I id) {
		E entity = findByIdForUpdateImpl(id);
		return entity;
	}

	protected abstract E findByIdForUpdateImpl(I id);

	@Override
	protected E findByIdFromStore(I id) {
		return findByIdImpl(id);
	}

	protected abstract E findByIdImpl(I id);

	@Override
	protected E saveIfAbsentToStore(I id, E entity) {
		entity = saveIfAbsentImpl(id, entity);
		return entity;
	}

	// 存完要获得锁，相当于随后findByIdForUpdate
	protected abstract E saveIfAbsentImpl(I id, E entity);

	@Override
	protected void removeAllToStore(Set<I> ids) {
		if (ids.isEmpty()) {
			return;
		}
		if (ids.size() == 1) {
			removeImpl(ids.iterator().next());
		} else {
			removeBatchImpl(ids);
		}
	}

	@Override
	protected void updateAllToStore(Map<I, E> entities) {
		I oneId = null;
		E oneEntity = null;
		for (Entry<I, E> entry : entities.entrySet()) {
			I id = entry.getKey();
			oneId = id;
			E entity = entry.getValue();
			oneEntity = entity;
		}

		if (entities.isEmpty()) {
			return;
		}
		if (entities.size() == 1) {
			updateImpl(oneId, oneEntity);
		} else {
			updateBatchImpl(entities);
		}
	}

	protected abstract void updateBatchImpl(Map<I, E> entitiesToUpdate);

	protected abstract void updateImpl(I id, E entity);

	@Override
	protected void saveAllToStore(Map<I, E> entities) {
		if (entities.isEmpty()) {
			return;
		}
		if (entities.size() == 1) {
			I oneId = null;
			E oneEntity = null;
			for (I id : entities.keySet()) {
				oneId = id;
				oneEntity = entities.get(id);
			}
			saveImpl(oneId, oneEntity);
		} else {
			saveBatchImpl(entities);
		}
	}

	@Override
	protected void unlockAllToStore(Set<I> ids) {
		if (ids.isEmpty()) {
			return;
		}
		if (ids.size() == 1) {
			unlockImpl(ids.iterator().next());
		} else {
			unlockBatchImpl(ids);
		}
	}

	protected abstract void saveBatchImpl(Map<I, E> entities);

	protected abstract void saveImpl(I id, E entity);

	// 别忘了也要一并删除锁
	protected abstract void removeBatchImpl(Set<I> ids);

	// 别忘了也要一并删除锁
	protected abstract void removeImpl(I id);

	protected abstract void unlockBatchImpl(Set<I> ids);

	protected abstract void unlockImpl(I id);

}
