package arp.store;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import arp.core.Store;
import arp.store.compare.EntityComparator;
import arp.store.copy.EntityCopier;

public abstract class PersistenceStore<I, T> implements Store<I, T> {

	private Map<I, T> originalEntities = new ConcurrentHashMap<>();

	@Override
	public T findForTake(I id) {
		T entity = findAndLock(id);
		originalEntities.put(id, EntityCopier.copy(entity));
		return entity;
	}

	protected abstract T findAndLock(I id);

	@Override
	public T findForRemoveReturn(I id) {
		return findAndLock(id);
	}

	@Override
	public void checkAndUpdateAll(Map<I, T> entities) {
		Map<I, T> entitiesToUpdate = new HashMap<>();
		for (Entry<I, T> entry : entities.entrySet()) {
			I id = entry.getKey();
			T entity = entry.getValue();
			if (!EntityComparator.equals(entity, originalEntities.remove(id))) {
				entitiesToUpdate.put(id, entity);
			}
		}
		updateAndUnlockAll(entitiesToUpdate);
	}

	protected abstract void updateAndUnlockAll(Map<I, T> entities);

}
