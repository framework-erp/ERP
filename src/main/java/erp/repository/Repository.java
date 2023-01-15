package erp.repository;

import erp.AppContext;
import erp.process.ProcessContext;
import erp.process.ProcessEntity;
import erp.process.ThreadBoundProcessContextArray;
import erp.util.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 仓库是存放实体的地方，实体只会通过它的id来获取。
 *
 * @param <E>  实体类型
 * @param <ID> ID类型
 */
public abstract class Repository<E, ID> {
    protected String entityType;

    protected Store<E, ID> store;
    protected Mutexes<ID> mutexes;

    private EntityIdGetter entityIdGetter = null;

    protected Repository(Store<E, ID> store, Mutexes<ID> mutexes) {
        Type genType = getClass().getGenericSuperclass();
        Type paramsType = ((ParameterizedType) genType).getActualTypeArguments()[0];
        entityType = paramsType.getTypeName();
        this.store = store;
        this.mutexes = mutexes;
        AppContext.registerRepository(entityType, store, mutexes);
    }

    public Repository(Store<E, ID> store, Mutexes<ID> mutexes, Class<E> entityType) {
        this.entityType = entityType.getName();
        this.store = store;
        this.mutexes = mutexes;
        AppContext.registerRepository(this.entityType, store, mutexes);
    }

    protected ID getId(E entity) {
        if (entityIdGetter == null) {
            try {
                createEntityIdGetter(entity);
            } catch (Exception e) {
                return null;
            }
        }
        return (ID) entityIdGetter.getId(entity);
    }

    public E take(ID id) {
        ProcessContext processContext = ThreadBoundProcessContextArray.getProcessContext();
        if (processContext == null || !processContext.isStarted()) {
            throw new RuntimeException("can not take from repository without a process");
        }

        ProcessEntity<E> processEntity = processContext.takeEntityInProcess(entityType, id);
        if (processEntity != null) {
            if (processEntity.isAvailable()) {
                return processEntity.getEntity();
            } else {
                return null;
            }
        }

        int lockRslt = mutexes.lock(id, processContext.getProcessName());
        E existsEntity;
        if (lockRslt == -1) {
            //检查entity存在且补锁
            existsEntity = find(id);
            if (existsEntity == null) {
                return null;
            }
            boolean ok = mutexes.newAndLock(id, processContext.getProcessName());
            if (!ok) {
                //补锁不成功那就是有人抢先补锁，那么这里就需要再去获得锁了
                lockRslt = mutexes.lock(id, processContext.getProcessName());
                if (lockRslt == 0) {
                    throw new TakeEntityException(mutexes.getLockProcess(id));
                }
            }
        } else {
            if (lockRslt == 0) {
                throw new TakeEntityException(mutexes.getLockProcess(id));
            }
            existsEntity = find(id);
            if (existsEntity == null) {
                return null;
            }
        }
        processContext.addEntityTakenFromRepo(entityType, id, existsEntity);
        return existsEntity;
    }

    public E find(ID id) {
        ProcessContext processContext = ThreadBoundProcessContextArray.getProcessContext();
        if (processContext != null && processContext.isStarted()) {
            E entity = processContext.copyEntityInProcess(entityType, id);
            if (entity != null) {
                return entity;
            }
        }
        return store.load(id);
    }

    public void put(E entity) {
        ProcessContext processContext = ThreadBoundProcessContextArray.getProcessContext();
        if (processContext == null || !processContext.isStarted()) {
            throw new RuntimeException("can not put to repository without a process");
        }
        ID id = getId(entity);
        if (processContext.entityAvailableInProcess(entityType, id)) {
            throw new RuntimeException("can not 'Put' since entity already exists");
        }
        processContext.addNewEntity(entityType, id, entity);
    }

    public PutIfAbsentResult<E> putIfAbsent(E entity) {
        ProcessContext processContext = ThreadBoundProcessContextArray.getProcessContext();
        if (processContext == null || !processContext.isStarted()) {
            throw new RuntimeException("can not put to repository without a process");
        }

        ID id = getId(entity);
        //先要看过程中的，如果有可用的那就拿来做实际值，如果有但是不可用那就取用新值且新值覆盖老值
        ProcessEntity<E> processEntity = processContext.getEntityInProcess(entityType, id);
        if (processEntity != null) {
            processEntity.changeStateByPutIfAbsent();
            if (processEntity.isAvailable()) {
                return new PutIfAbsentResult(processEntity.getEntity(), false);
            } else {
                processEntity.setEntity(entity);
                return new PutIfAbsentResult(entity, true);
            }
        }

        boolean ok = mutexes.newAndLock(id, processContext.getProcessName());
        if (!ok) {
            E actual = take(id);
            return new PutIfAbsentResult(actual, false);
        }
        store.insert(id, entity);
        processContext.addEntityTakenFromRepo(entityType, id, entity);
        return new PutIfAbsentResult(entity, true);
    }

    public E remove(ID id) {
        E entity = take(id);
        if (entity != null) {
            ProcessContext processContext = ThreadBoundProcessContextArray.getProcessContext();
            processContext.removeEntityInProcess(entityType, id);
        }
        return entity;
    }

    private void createEntityIdGetter(E entity) throws Exception {
        Field idField = entity.getClass().getDeclaredField("id");
        long idFieldOffset = Unsafe.getFieldOffset(idField);
        Class<?> idFieldType = idField.getType();
        if (idFieldType.equals(byte.class)) {
            entityIdGetter = (e) -> {
                return Unsafe.getByteFieldOfObject(e, idFieldOffset);
            };
        } else if (idFieldType.equals(short.class)) {
            entityIdGetter = (e) -> {
                return Unsafe.getShortFieldOfObject(e, idFieldOffset);
            };
        } else if (idFieldType.equals(char.class)) {
            entityIdGetter = (e) -> {
                return Unsafe.getCharFieldOfObject(e, idFieldOffset);
            };
        } else if (idFieldType.equals(int.class)) {
            entityIdGetter = (e) -> {
                return Unsafe.getIntFieldOfObject(e, idFieldOffset);
            };
        } else if (idFieldType.equals(float.class)) {
            entityIdGetter = (e) -> {
                return Unsafe.getFloatFieldOfObject(e, idFieldOffset);
            };
        } else if (idFieldType.equals(long.class)) {
            entityIdGetter = (e) -> {
                return Unsafe.getLongFieldOfObject(e, idFieldOffset);
            };
        } else if (idFieldType.equals(double.class)) {
            entityIdGetter = (e) -> {
                return Unsafe.getDoubleFieldOfObject(e, idFieldOffset);
            };
        } else if (idFieldType.equals(boolean.class)) {
            entityIdGetter = (e) -> {
                return Unsafe.getBooleanFieldOfObject(e, idFieldOffset);
            };
        } else {
            entityIdGetter = (e) -> {
                return Unsafe.getObjectFieldOfObject(e, idFieldOffset);
            };
        }

    }

}

interface EntityIdGetter {
    Object getId(Object entity);
}