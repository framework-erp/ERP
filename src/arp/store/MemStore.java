package arp.store;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

import arp.core.Store;
import arp.store.copy.EntityCopier;

public class MemStore<ID, T> implements Store<ID, T> {

	private Map<ID, T> data = new ConcurrentHashMap<>();

	private Map<ID, AtomicInteger> locks = new ConcurrentHashMap<>();

	@Override
	public T findForTake(ID id) {
		acquireLock(id);
		return data.get(id);
	}

	@Override
	public T findForRead(ID id) {
		return EntityCopier.copy(data.get(id));
	}

	@Override
	public T createIfAbsentAndTake(ID id, T entity) {
		acquireLock(id);
		T existsEntity = data.putIfAbsent(id, entity);
		if (existsEntity != null) {
			return existsEntity;
		} else {
			return entity;
		}
	}

	@Override
	public void checkAndUpdateAll(Map<ID, T> entities) {
		for (Entry<ID, T> entry : entities.entrySet()) {
			ID id = entry.getKey();
			data.put(id, EntityCopier.copy(entry.getValue()));
			unlock(id);
		}
	}

	@Override
	public void createAll(Map<ID, T> entities) {
		for (Entry<ID, T> entry : entities.entrySet()) {
			data.put(entry.getKey(), EntityCopier.copy(entry.getValue()));
		}
	}

	@Override
	public void removeAll(Set<ID> ids) {
		for (ID id : ids) {
			data.remove(id);
			locks.remove(id);
		}
	}

	@Override
	public void returnAll(Set<ID> ids) {
		for (ID id : ids) {
			unlock(id);
		}
	}

	private void unlock(ID id) {
		AtomicInteger lock = locks.get(id);
		if (lock != null) {
			lock.set(0);
		}
	}

	private void acquireLock(ID id) {
		AtomicInteger lock = locks.get(id);
		if (lock == null) {
			AtomicInteger newLock = new AtomicInteger();
			lock = locks.putIfAbsent(id, newLock);
			if (lock == null) {
				lock = newLock;
			}
		}

		int tid = (int) Thread.currentThread().getId();
		if (lock.get() == tid) {
			return;
		}

		int counter = 200;
		do {
			if (lock.compareAndSet(0, tid)) {
				return;
			}

			if (counter > 100) {
				--counter;
			} else if (counter > 0) {
				--counter;
				Thread.yield();
			} else {
				LockSupport.parkNanos(1L);
			}
		} while (true);
	}

}
