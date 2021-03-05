package arp.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import arp.repository.compare.EntityComparator;
import arp.repository.copy.EntityCopier;

public abstract class PersistenceRepository<E, ID> extends Repository<E, ID> {

	private Map<ID, E> originalEntities = new ConcurrentHashMap<>();

	@Override
	protected E findByIdForUpdateFromStore(ID id) {
		E entity = findByIdForUpdateImpl(id);
		if (entity != null) {
			originalEntities.put(id, EntityCopier.copy(entity));
		}
		return entity;
	}

	protected abstract E findByIdForUpdateImpl(ID id);

	@Override
	protected E findByIdFromStore(ID id) {
		return findByIdImpl(id);
	}

	protected abstract E findByIdImpl(ID id);

	@Override
	protected E saveIfAbsentToStore(ID id, E entity) {
		entity = saveIfAbsentImpl(id, entity);
		if (entity != null) {
			originalEntities.put(id, EntityCopier.copy(entity));
		}
		return entity;
	}

	// 存完要获得锁，相当于随后findByIdForUpdate
	protected abstract E saveIfAbsentImpl(ID id, E entity);

	@Override
	protected void removeAllToStore(Set<ID> ids) {
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
	protected void updateAllToStore(Map<ID, E> entities) {
		Map<ID, E> entitiesToUpdate = new HashMap<>();
		ID oneId = null;
		E oneEntity = null;
		for (Entry<ID, E> entry : entities.entrySet()) {
			ID id = entry.getKey();
			oneId = id;
			E entity = entry.getValue();
			oneEntity = entity;
			if (!EntityComparator.equals(entity, originalEntities.remove(id))) {
				entitiesToUpdate.put(id, entity);
			}
		}

		if (entitiesToUpdate.isEmpty()) {
			return;
		}
		if (entitiesToUpdate.size() == 1) {
			updateAndUnlockImpl(oneId, oneEntity);
		} else {
			updateAndUnlockBatchImpl(entitiesToUpdate);
		}
	}

	protected abstract void updateAndUnlockBatchImpl(Map<ID, E> entitiesToUpdate);

	protected abstract void updateAndUnlockImpl(ID id, E entity);

	@Override
	protected void saveAllToStore(Map<ID, E> entities) {
		if (entities.isEmpty()) {
			return;
		}
		if (entities.size() == 1) {
			ID oneId = null;
			E oneEntity = null;
			for (ID id : entities.keySet()) {
				oneId = id;
				oneEntity = entities.get(id);
			}
			saveImpl(oneId, oneEntity);
		} else {
			saveBatchImpl(entities);
		}
	}

	@Override
	protected void unlockAllToStore(Set<ID> ids) {
		if (ids.isEmpty()) {
			return;
		}
		if (ids.size() == 1) {
			unlockImpl(ids.iterator().next());
		} else {
			unlockBatchImpl(ids);
		}
	}

	protected abstract void saveBatchImpl(Map<ID, E> entities);

	protected abstract void saveImpl(ID id, E entity);

	// 别忘了也要一并删除锁
	protected abstract void removeBatchImpl(Set<ID> ids);

	// 别忘了也要一并删除锁
	protected abstract void removeImpl(ID id);

	protected abstract void unlockBatchImpl(Set<ID> ids);

	protected abstract void unlockImpl(ID id);

}
