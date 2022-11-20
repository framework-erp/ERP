package arp.repository;

import arp.process.*;
import arp.util.Unsafe;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * 仓库是存放聚合的地方，聚合只会通过它的id来获取。
 *
 * @param <E>  实体类型
 * @param <ID> ID类型
 */
public abstract class Repository<E, ID> {

    private Store<E, ID> store;
    private Mutexes<ID> mutexes;

    private static AtomicInteger ids = new AtomicInteger();

    private static Repository[] repositories = new Repository[1024];

    private int id;

    private Function<Object, Object> getEntityIdFunction = null;

    private Function<Object[], Object> setEntityIdFunction = null;

    private Map<ID, E> mockStore;

    protected boolean mock = false;

    public static Repository getRepository(int id) {
        return repositories[id];
    }

    protected Repository() {
        id = ids.incrementAndGet();
        repositories[id] = this;
    }

    protected void initAsMock() {
        this.mock = true;
        mockStore = new HashMap<>();
    }

    private ID getId(E entity) {
        if (getEntityIdFunction == null) {
            try {
                createGetEntityIdFunction(entity);
            } catch (Exception e) {
                return null;
            }
        }
        return (ID) getEntityIdFunction.apply(entity);
    }

    public E take(ID id) {
        ProcessContext processContext = ThreadBoundProcessContextArray.getProcessContext();
        if (processContext == null) {
            throw new RuntimeException("can not take from repository without a process");
        }

        ProcessEntity<E> processEntity = processContext.takeEntityInProcess(this.id, id);
        if (processEntity != null) {
            if (processEntity.isAvailable()) {
                return processEntity.getEntity();
            } else {
                return null;
            }
        }

        int lockRslt = mutexes.lock(id);
        E existsEntity;
        if (lockRslt == -1) {
            //检查entity存在且补锁
            existsEntity = find(id);
            if (existsEntity == null) {
                return null;
            }
            boolean ok = mutexes.newAndLock(id);
            if (!ok) {
                //补锁不成功那就是有人抢先补锁，那么这里就需要再去获得锁了
                lockRslt = mutexes.lock(id);
                if (lockRslt == 0) {
                    throw new CanNotAcquireLockException(mutexes.getLockProcess());
                }
            }
        } else {
            if (lockRslt == 0) {
                throw new CanNotAcquireLockException(mutexes.getLockProcess());
            }
            existsEntity = find(id);
            if (existsEntity == null) {
                return null;
            }
        }
        processContext.addEntityTakenFromRepo(this.id, id, existsEntity);
        return existsEntity;
    }

    public E find(ID id) {
        ProcessContext processContext = ThreadBoundProcessContextArray.getProcessContext();
        if (processContext != null) {
            E entity = processContext.copyEntityInProcess(this.id, id);
            if (entity != null) {
                return entity;
            }
        }
        return store.load(id);
    }

    public void put(E entity) {
        ProcessContext processContext = ThreadBoundProcessContextArray.getProcessContext();
        if (processContext == null) {
            throw new RuntimeException("can not put to repository without a process");
        }
        if (processContext.entityAvailableInProcess(this.id, id)) {
            throw new RuntimeException("can not 'Put' since entity already exists");
        }
        ID id = getId(entity);
        processContext.addNewEntity(this.id, id, entity);
    }

    public E putIfAbsent(E entity) {
        ProcessContext processContext = ThreadBoundProcessContextArray.getProcessContext();
        if (!processContext.isStarted()) {
            throw new RuntimeException("can not use repository without a process");
        }

        ID id = getId(entity);

        ProcessEntity<E> processEntity = processContext.putIfAbsentEntityInProcess(this.id, id, entity);
        if (processEntity != null) {
            ProcessEntityState entityState = processEntity.getState();
            if (!(entityState instanceof TransientProcessEntityState)) {
                return processEntity.getEntity();
            }
        }

        E entityFromStore = doSaveIfAbsent(id, entity);
        if (entityFromStore != null) {
            processContext.addEntityTakenFromRepo(this.id, id, entityFromStore);
        } else {
            processContext.addCreatedAggr(entity);
            processContext.addEntityTakenFromRepo(this.id, id, entity);
        }
        return entityFromStore;
    }

    private E doSaveIfAbsent(ID id, E entity) {
        if (!mock) {
            return saveIfAbsentToStore(id, entity);
        } else {
            return mockStore.putIfAbsent(id, entity);
        }
    }

    protected abstract E saveIfAbsentToStore(ID id, E entity);

    public E remove(ID id) {
        ProcessContext processContext = ThreadBoundProcessContextArray.getProcessContext();
        if (!processContext.isStarted()) {
            throw new RuntimeException("can not use repository without a process");
        }
        ProcessEntity<E> processEntity = processContext.removeEntityInProcess(this.id, id);
        if (processEntity != null) {
            return processEntity.getEntity();
        }

        E entityFromStore = doFindByIdForUpdate(id);
        if (entityFromStore != null) {
            processContext.takeEntityFromRepoAndPutInProcessAsRemoved(this.id, id, entityFromStore);
        }
        return entityFromStore;

    }

    public void deleteEntities(Set<ID> ids) {

        if (!mock) {
            removeAllToStore(ids);
        } else {
            for (ID id : ids) {
                mockStore.remove(id);
            }
        }

    }

    protected abstract void removeAllToStore(Set<ID> ids);

    public void updateEntities(Map<ID, E> entitiesToReturn) {

        if (!mock) {
            updateAllToStore(entitiesToReturn);
        } else {
        }

    }

    protected abstract void updateAllToStore(Map<ID, E> entities);

    public void createEntities(Map<ID, E> entitiesToCreate) {

        if (!mock) {
            saveAllToStore(entitiesToCreate);
        } else {
            mockStore.putAll(entitiesToCreate);
        }

    }

    protected abstract void saveAllToStore(Map<ID, E> entities);

    public void returnEntities(Set<ID> ids) {
        if (!mock) {
            unlockAllToStore(ids);
        } else {
        }
    }

    protected abstract void unlockAllToStore(Set<ID> ids);

    public E findByIdForUpdateOrCreateAndLock(ID id, E newEntity) {
        E entity = take(id);
        if (entity == null) {
            if (setEntityIdFunction == null) {
                try {
                    createSetEntityIdFunction(newEntity);
                } catch (Exception e) {
                }
            }
            setEntityIdFunction.apply(new Object[]{newEntity, id});
            E existsEntity = putIfAbsent(newEntity);
            if (existsEntity != null) {
                return existsEntity;
            }
            return newEntity;
        }
        return entity;
    }

    private void createGetEntityIdFunction(E entity) throws Exception {
        Field idField = entity.getClass().getDeclaredField("id");
        long idFieldOffset = Unsafe.getFieldOffset(idField);
        Class<?> idFieldType = idField.getType();
        if (idFieldType.equals(byte.class)) {
            getEntityIdFunction = new Function<Object, Object>() {

                @Override
                public Object apply(Object t) {
                    return Unsafe.getByteFieldOfObject(t, idFieldOffset);
                }

            };
        } else if (idFieldType.equals(short.class)) {
            getEntityIdFunction = new Function<Object, Object>() {

                @Override
                public Object apply(Object t) {
                    return Unsafe.getShortFieldOfObject(t, idFieldOffset);
                }

            };
        } else if (idFieldType.equals(char.class)) {
            getEntityIdFunction = new Function<Object, Object>() {

                @Override
                public Object apply(Object t) {
                    return Unsafe.getCharFieldOfObject(t, idFieldOffset);
                }

            };
        } else if (idFieldType.equals(int.class)) {
            getEntityIdFunction = new Function<Object, Object>() {

                @Override
                public Object apply(Object t) {
                    return Unsafe.getIntFieldOfObject(t, idFieldOffset);
                }

            };
        } else if (idFieldType.equals(float.class)) {
            getEntityIdFunction = new Function<Object, Object>() {

                @Override
                public Object apply(Object t) {
                    return Unsafe.getFloatFieldOfObject(t, idFieldOffset);
                }

            };
        } else if (idFieldType.equals(long.class)) {
            getEntityIdFunction = new Function<Object, Object>() {

                @Override
                public Object apply(Object t) {
                    return Unsafe.getLongFieldOfObject(t, idFieldOffset);
                }

            };
        } else if (idFieldType.equals(double.class)) {
            getEntityIdFunction = new Function<Object, Object>() {

                @Override
                public Object apply(Object t) {
                    return Unsafe.getDoubleFieldOfObject(t, idFieldOffset);
                }

            };
        } else if (idFieldType.equals(boolean.class)) {
            getEntityIdFunction = new Function<Object, Object>() {

                @Override
                public Object apply(Object t) {
                    return Unsafe.getBooleanFieldOfObject(t, idFieldOffset);
                }

            };
        } else {
            getEntityIdFunction = new Function<Object, Object>() {

                @Override
                public Object apply(Object t) {
                    return Unsafe.getObjectFieldOfObject(t, idFieldOffset);
                }

            };
        }

    }

    private void createSetEntityIdFunction(E entity) throws Exception {
        Field idField = entity.getClass().getDeclaredField("id");
        long idFieldOffset = Unsafe.getFieldOffset(idField);
        Class<?> idFieldType = idField.getType();
        if (idFieldType.equals(byte.class)) {
            setEntityIdFunction = new Function<Object[], Object>() {

                @Override
                public Object apply(Object[] t) {
                    Unsafe.setByteFieldOfObject(t[0], idFieldOffset, ((Byte) t[1]).byteValue());
                    return null;
                }

            };
        } else if (idFieldType.equals(short.class)) {
            setEntityIdFunction = new Function<Object[], Object>() {

                @Override
                public Object apply(Object[] t) {
                    Unsafe.setShortFieldOfObject(t[0], idFieldOffset, ((Short) t[1]).shortValue());
                    return null;
                }

            };
        } else if (idFieldType.equals(char.class)) {
            setEntityIdFunction = new Function<Object[], Object>() {

                @Override
                public Object apply(Object[] t) {
                    Unsafe.setCharFieldOfObject(t[0], idFieldOffset, ((Character) t[1]).charValue());
                    return null;
                }

            };
        } else if (idFieldType.equals(int.class)) {
            setEntityIdFunction = new Function<Object[], Object>() {

                @Override
                public Object apply(Object[] t) {
                    Unsafe.setIntFieldOfObject(t[0], idFieldOffset, ((Integer) t[1]).intValue());
                    return null;
                }

            };
        } else if (idFieldType.equals(float.class)) {
            setEntityIdFunction = new Function<Object[], Object>() {

                @Override
                public Object apply(Object[] t) {
                    Unsafe.setFloatFieldOfObject(t[0], idFieldOffset, ((Float) t[1]).floatValue());
                    return null;
                }

            };
        } else if (idFieldType.equals(long.class)) {
            setEntityIdFunction = new Function<Object[], Object>() {

                @Override
                public Object apply(Object[] t) {
                    Unsafe.setLongFieldOfObject(t[0], idFieldOffset, ((Long) t[1]).longValue());
                    return null;
                }

            };
        } else if (idFieldType.equals(double.class)) {
            setEntityIdFunction = new Function<Object[], Object>() {

                @Override
                public Object apply(Object[] t) {
                    Unsafe.setDoubleFieldOfObject(t[0], idFieldOffset, ((Double) t[1]).doubleValue());
                    return null;
                }

            };
        } else if (idFieldType.equals(boolean.class)) {
            setEntityIdFunction = new Function<Object[], Object>() {

                @Override
                public Object apply(Object[] t) {
                    Unsafe.setBooleanFieldOfObject(t[0], idFieldOffset, ((Boolean) t[1]).booleanValue());
                    return null;
                }

            };
        } else {
            setEntityIdFunction = new Function<Object[], Object>() {

                @Override
                public Object apply(Object[] t) {
                    Unsafe.setObjectFieldOfObject(t[0], idFieldOffset, t[1]);
                    return null;
                }

            };
        }

    }
}
