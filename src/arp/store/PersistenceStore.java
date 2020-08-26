package arp.store;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import arp.core.Store;
import arp.store.compare.EntityComparator;
import arp.store.copy.EntityCopier;

public abstract class PersistenceStore<ID, T> implements Store<ID, T> {

	private Map<ID, T> originalEntities = new ConcurrentHashMap<>();

	@Override
	public T findForTake(ID id) {
		T entity = findAndLock(id);
		originalEntities.put(id, EntityCopier.copy(entity));
		return entity;
	}

	protected abstract T findAndLock(ID id);

	// 有返回，相当于随后findForTake
	@Override
	public T createIfAbsentAndTake(ID id, T entity) {
		T entityTaken = createIfAbsentAndLock(id, entity);
		originalEntities.put(id, EntityCopier.copy(entityTaken));
		return entityTaken;
	}

	protected abstract T createIfAbsentAndLock(ID id, T entity);

	@Override
	public void checkAndUpdateAll(Map<ID, T> entities) {
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
			updateAndUnlock(oneId, oneEntity);
		} else {
			updateAndUnlockBatch(entitiesToUpdate);
		}

	}

	protected abstract void updateAndUnlockBatch(Map<ID, T> entitiesToUpdate);

	protected abstract void updateAndUnlock(ID id, T entity);

	@Override
	public void createAll(Map<ID, T> entities) {
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
			create(oneId, oneEntity);
		} else {
			createBatch(entities);
		}
	}

	protected abstract void createBatch(Map<ID, T> entities);

	protected abstract void create(ID id, T entity);

	@Override
	public void removeAll(Set<ID> ids) {
		if (ids.isEmpty()) {
			return;
		}
		if (ids.size() == 1) {
			remove(ids.iterator().next());
		} else {
			removeBatch(ids);
		}
	}

	// 别忘了也要一并删除锁
	protected abstract void removeBatch(Set<ID> ids);

	// 别忘了也要一并删除锁
	protected abstract void remove(ID id);

	@Override
	public void returnAll(Set<ID> ids) {
		if (ids.isEmpty()) {
			return;
		}
		if (ids.size() == 1) {
			unlock(ids.iterator().next());
		} else {
			unlockBatch(ids);
		}
	}

	protected abstract void unlockBatch(Set<ID> ids);

	protected abstract void unlock(ID id);

}
