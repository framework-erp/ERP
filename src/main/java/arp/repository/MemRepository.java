package arp.repository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

import arp.repository.copy.EntityCopier;

public abstract class MemRepository<E, I> extends Repository<E, I> {

	private Map<I, E> data = new ConcurrentHashMap<>();

	private Map<I, AtomicInteger> locks = new ConcurrentHashMap<>();

	private void unlock(I id) {
		AtomicInteger lock = locks.get(id);
		if (lock != null) {
			lock.set(0);
		}
	}

	private void acquireLock(I id) {
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
	protected E findByIdForUpdateFromStore(I id) {
		if (data.get(id) == null) {
			return null;
		}
		acquireLock(id);
		return data.get(id);
	}

	@Override
	protected E findByIdFromStore(I id) {
		return EntityCopier.copy(data.get(id));
	}

	@Override
	protected E saveIfAbsentToStore(I id, E entity) {
		acquireLock(id);
		E existsEntity = data.putIfAbsent(id, entity);
		return existsEntity;
	}

	@Override
	protected void removeAllToStore(Set<I> ids) {
		for (I id : ids) {
			data.remove(id);
			locks.remove(id);
		}
	}

	@Override
	protected void updateAllToStore(Map<I, E> entities) {
		for (Entry<I, E> entry : entities.entrySet()) {
			I id = entry.getKey();
			data.put(id, EntityCopier.copy(entry.getValue()));
			unlock(id);
		}
	}

	@Override
	protected void saveAllToStore(Map<I, E> entities) {
		for (Entry<I, E> entry : entities.entrySet()) {
			data.put(entry.getKey(), EntityCopier.copy(entry.getValue()));
		}
	}

	@Override
	protected void unlockAllToStore(Set<I> ids) {
		for (I id : ids) {
			unlock(id);
		}
	}

	public Set<I> idSet() {
		return new HashSet<>(data.keySet());
	}

	public List<E> findAll() {
		return new ArrayList<>(data.values());
	}

}
