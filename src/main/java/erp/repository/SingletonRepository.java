package erp.repository;

import erp.AppContext;
import erp.process.ProcessContext;
import erp.process.ThreadBoundProcessContextArray;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

/**
 * 保存的是一个不存在于某个集合当中的单例的实体。只在内存中，如需从数据库加载初始数据，则在系统启动时完成加载
 */
public class SingletonRepository<T> {

    private String name;

    private T entity;

    private AtomicInteger lock = new AtomicInteger();

    private long acquireLockThreadId = -1;

    public SingletonRepository(String repositoryName) {
        this.name = repositoryName;
        AppContext.registerSingletonRepository(this);
    }

    public SingletonRepository(T entity) {
        this(entity.getClass().getName());
        this.entity = entity;
    }

    public SingletonRepository(T entity, String repositoryName) {
        this(repositoryName);
        this.entity = entity;
    }

    protected SingletonRepository() {
        Type genType = getClass().getGenericSuperclass();
        Type paramsType = ((ParameterizedType) genType).getActualTypeArguments()[0];
        name = paramsType.getTypeName();
        AppContext.registerSingletonRepository(this);
    }


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
        if (Thread.currentThread().getId() == acquireLockThreadId) {
            return;
        }
        int counter = 200;
        do {
            if (lock.compareAndSet(0, 1)) {
                acquireLockThreadId = Thread.currentThread().getId();
                processContext.addEntityTakenFromSingletonRepo(name);
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

    public void releaseLock() {
        lock.set(0);
        acquireLockThreadId = -1;
    }

    public String getName() {
        return name;
    }
}
