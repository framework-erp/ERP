package arp.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import arp.core.Repository;
import arp.repository.compare.EntityComparator;
import arp.repository.copy.EntityCopier;

public abstract class PersistenceRepository<ID, T> extends Repository<ID, T> {

	private Map<ID, T> originalEntities = new ConcurrentHashMap<>();

	protected PersistenceRepository() {
	}

	protected PersistenceRepository(boolean mock) {
		super(mock);
	}

	@Override
	protected T findByIdForUpdateFromStore(ID id) {
		T entity = findByIdForUpdateImpl(id);
		originalEntities.put(id, EntityCopier.copy(entity));
		return entity;
	}

	protected abstract T findByIdForUpdateImpl(ID id);

	@Override
	protected T findByIdFromStore(ID id) {
		return findByIdImpl(id);
	}

	protected abstract T findByIdImpl(ID id);

	@Override
	protected T saveIfAbsentToStore(ID id, T entity) {
		T entityTaken = saveIfAbsentImpl(id, entity);
		originalEntities.put(id, EntityCopier.copy(entityTaken));
		return entityTaken;
	}

	// 有返回，相当于随后findByIdForUpdate
	protected abstract T saveIfAbsentImpl(ID id, T entity);

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
	protected void updateAllToStore(Map<ID, T> entities) {
		Map<ID, T> entitiesToUpdate = new HashMap<>();
		ID oneId = null;
		T oneEntity = null;
		for (Entry<ID, T> entry : entities.entrySet()) {
			ID id = entry.getKey();
			oneId = id;
			T entity = entry.getValue();
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

	protected abstract void updateAndUnlockBatchImpl(Map<ID, T> entitiesToUpdate);

	protected abstract void updateAndUnlockImpl(ID id, T entity);

	@Override
	protected void saveAllToStore(Map<ID, T> entities) {
		if (entities.isEmpty()) {
			return;
		}
		if (entities.size() == 1) {
			ID oneId = null;
			T oneEntity = null;
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

	protected abstract void saveBatchImpl(Map<ID, T> entities);

	protected abstract void saveImpl(ID id, T entity);

	// 别忘了也要一并删除锁
	protected abstract void removeBatchImpl(Set<ID> ids);

	// 别忘了也要一并删除锁
	protected abstract void removeImpl(ID id);

	protected abstract void unlockBatchImpl(Set<ID> ids);

	protected abstract void unlockImpl(ID id);

}
