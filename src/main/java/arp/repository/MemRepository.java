package arp.repository;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

import arp.repository.copy.EntityCopier;

public abstract class MemRepository<E, ID> extends Repository<E, ID> {

	private Map<ID, E> data = new ConcurrentHashMap<>();

	private Map<ID, AtomicInteger> locks = new ConcurrentHashMap<>();

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

		int counter = 300;
		do {
			if (lock.compareAndSet(0, tid)) {
				return;
			}

			if (counter > 200) {
				--counter;
			} else if (counter > 100) {
				--counter;
				Thread.yield();
			} else if (counter > 0) {
				--counter;
				LockSupport.parkNanos(1L);
			} else {
				throw new CanNotAcquireLockException();
			}
		} while (true);
	}

	@Override
	protected E findByIdForUpdateFromStore(ID id) {
		if (data.get(id) == null) {
			return null;
		}
		acquireLock(id);
		return data.get(id);
	}

	@Override
	protected E findByIdFromStore(ID id) {
		return EntityCopier.copy(data.get(id));
	}

	@Override
	protected E saveIfAbsentToStore(ID id, E entity) {
		acquireLock(id);
		E existsEntity = data.putIfAbsent(id, entity);
		return existsEntity;
	}

	@Override
	protected void removeAllToStore(Set<ID> ids) {
		for (ID id : ids) {
			data.remove(id);
			locks.remove(id);
		}
	}

	@Override
	protected void updateAllToStore(Map<ID, E> entities) {
		for (Entry<ID, E> entry : entities.entrySet()) {
			ID id = entry.getKey();
			data.put(id, EntityCopier.copy(entry.getValue()));
			unlock(id);
		}
	}

	@Override
	protected void saveAllToStore(Map<ID, E> entities) {
		for (Entry<ID, E> entry : entities.entrySet()) {
			data.put(entry.getKey(), EntityCopier.copy(entry.getValue()));
		}
	}

	@Override
	protected void unlockAllToStore(Set<ID> ids) {
		for (ID id : ids) {
			unlock(id);
		}
	}

	public Set<ID> idSet() {
		return new HashSet<>(data.keySet());
	}

}
