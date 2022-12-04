package arp.repository;

import arp.AppContext;
import arp.process.*;
import arp.util.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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
    private String aggType;

    private Store<E, ID> store;
    private Mutexes<ID> mutexes;

    private Function<Object, Object> getEntityIdFunction = null;

    private Function<Object[], Object> setEntityIdFunction = null;

    protected Repository(Store<E, ID> store, Mutexes<ID> mutexes) {
        Type genType = getClass().getGenericSuperclass();
        Type paramsType = ((ParameterizedType) genType).getActualTypeArguments()[0];
        AppContext.registerRepository(paramsType.getTypeName(), store, mutexes);
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

        ProcessEntity<E> processEntity = processContext.takeEntityInProcess(aggType, id);
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
        processContext.addEntityTakenFromRepo(aggType, id, existsEntity);
        return existsEntity;
    }

    public E find(ID id) {
        ProcessContext processContext = ThreadBoundProcessContextArray.getProcessContext();
        if (processContext != null) {
            E entity = processContext.copyEntityInProcess(aggType, id);
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
        ID id = getId(entity);
        if (processContext.entityAvailableInProcess(aggType, id)) {
            throw new RuntimeException("can not 'Put' since entity already exists");
        }
        processContext.addNewEntity(aggType, id, entity);
    }

    public PutIfAbsentResult<E> putIfAbsent(E entity) {
        ProcessContext processContext = ThreadBoundProcessContextArray.getProcessContext();
        if (processContext == null) {
            throw new RuntimeException("can not put to repository without a process");
        }

        ID id = getId(entity);
        //先要看过程中的，如果有可用的那就拿来做实际值，如果有但是不可用那就取用新值且新值覆盖老值
        ProcessEntity<E> processEntity = processContext.getEntityInProcess(aggType, id);
        if (processEntity != null) {
            processEntity.changeStateByPutIfAbsent();
            if (processEntity.isAvailable()) {
                return new PutIfAbsentResult(processEntity.getEntity(), false);
            } else {
                processEntity.setEntity(entity);
                return new PutIfAbsentResult(entity, true);
            }
        }

        boolean ok = mutexes.newAndLock(id);
        if (!ok) {
            E actual = take(id);
            return new PutIfAbsentResult(actual, false);
        }
        store.save(id, entity);
        processContext.addEntityTakenFromRepo(aggType, id, entity);
        return new PutIfAbsentResult(entity, true);
    }

    public E remove(ID id) {
        E entity = take(id);
        if (entity != null) {
            ProcessContext processContext = ThreadBoundProcessContextArray.getProcessContext();
            processContext.removeEntityInProcess(aggType, id);
        }
        return entity;
    }

    public E takeOrPutIfAbsent(ID id, E newEntity) {
        E entity = take(id);
        if (entity == null) {
            return putIfAbsent(newEntity).getActual();
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
