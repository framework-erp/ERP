package arp.repository;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

import arp.process.ProcessContext;
import arp.process.ThreadBoundProcessContextArray;

/**
 * 保存的是一个不存在于某个集合当中的独立的实体。没有懒加载的场景，在系统启动时需要加载
 * 
 * @author neo
 *
 */
public abstract class SingleEntityRepository<T> {

	private AtomicInteger lock = new AtomicInteger();

	private T entity;

	public T getForUpdate() {
		ProcessContext processContext = ThreadBoundProcessContextArray.getProcessContext();
		if (!processContext.isStarted()) {
			throw new RuntimeException("can not use repository without a process");
		}
		acquireLock(processContext);
		return entity;
	}

	public T get() {
		return entity;
	}

	public void put(T entity) {
		this.entity = entity;
	}

	private void acquireLock(ProcessContext processContext) {
		int counter = 200;
		do {
			if (lock.compareAndSet(0, 1)) {
				processContext.addSingleEntityAcquiredLock(lock);
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
