package arp.repository;

import arp.AppContext;
import arp.process.ProcessContext;
import arp.process.ThreadBoundProcessContextArray;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

/**
 * 保存的是一个不存在于某个集合当中的独立的实体。只在内存中，如需从数据库加载初始数据，则在系统启动时完成加载
 */
public class SingletonRepository<T> {

    public SingletonRepository() {
        Type genType = getClass().getGenericSuperclass();
        Type paramsType = ((ParameterizedType) genType).getActualTypeArguments()[0];
        entityType = paramsType.getTypeName();
        AppContext.registerSingletonRepository(entityType, lock);
    }

    public SingletonRepository(T entity) {
        this();
        this.entity = entity;
    }

    private String entityType;

    private T entity;

    private AtomicInteger lock = new AtomicInteger();

    public T get() {
        return entity;
    }

    public T take() {
        ProcessContext processContext = ThreadBoundProcessContextArray.getProcessContext();
        if (processContext == null) {
            throw new RuntimeException("can not take from repository without a process");
        }
        acquireLock(processContext);
        return entity;
    }

    public void put(T entity) {
        this.entity = entity;
    }

    private void acquireLock(ProcessContext processContext) {
        int counter = 200;
        do {
            if (lock.compareAndSet(0, 1)) {
                processContext.addEntityTakenFromSingletonRepo(entityType);
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
