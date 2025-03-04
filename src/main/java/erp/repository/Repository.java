package erp.repository;

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
    protected String name;
    protected Class<E> entityType;
    protected String entityIDField;

    protected Store<E, ID> store;
    protected Mutexes<ID> mutexes;

    private EntityIdGetter entityIdGetter = null;

    protected Repository() {
        Type genType = getClass().getGenericSuperclass();
        Type paramsType = ((ParameterizedType) genType).getActualTypeArguments()[0];
        name = paramsType.getTypeName();
        try {
            createEntityIdGetter((Class<?>) paramsType);
        } catch (Exception e) {
            throw new RuntimeException("createEntityIdGetter error", e);
        }
        this.entityType = (Class<E>) paramsType;
    }

    protected Repository(String repositoryName) {
        Type genType = getClass().getGenericSuperclass();
        Type paramsType = ((ParameterizedType) genType).getActualTypeArguments()[0];
        name = repositoryName;
        try {
            createEntityIdGetter((Class<?>) paramsType);
        } catch (Exception e) {
            throw new RuntimeException("createEntityIdGetter error", e);
        }
        this.entityType = (Class<E>) paramsType;
    }

    protected Repository(Class<E> entityType) {
        this.name = entityType.getName();
        try {
            createEntityIdGetter(entityType);
        } catch (Exception e) {
            throw new RuntimeException("createEntityIdGetter error", e);
        }
        this.entityType = entityType;
    }

    protected Repository(Class<E> entityType, String repositoryName) {
        this.name = repositoryName;
        try {
            createEntityIdGetter(entityType);
        } catch (Exception e) {
            throw new RuntimeException("createEntityIdGetter error", e);
        }
        this.entityType = entityType;
    }

    protected ID getId(E entity) {
        return (ID) entityIdGetter.getId(entity);
    }

    public E take(ID id) {
        ProcessContext processContext = ThreadBoundProcessContextArray.getProcessContext();
        if (processContext == null || !processContext.isStarted()) {
            throw new RuntimeException("can not take from repository without a process");
        }

        ProcessEntity<E> processEntity = processContext.takeEntityInProcess(name, id);
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
                //补锁不成功那就是有人抢先补锁，那么这里就需要再去take(递归)
                return take(id);
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
        processContext.addEntityTakenFromRepo(name, id, existsEntity);
        return existsEntity;
    }

    public E find(ID id) {
        ProcessContext processContext = ThreadBoundProcessContextArray.getProcessContext();
        if (processContext != null && processContext.isStarted()) {
            E entity = processContext.copyEntityInProcess(name, id);
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
        if (processContext.entityAvailableInProcess(name, id)) {
            throw new RuntimeException("can not 'Put' since entity already exists");
        }
        processContext.addNewEntity(name, id, entity);
    }

    /**
     * @param entity
     * @return 已存在的实体，或者null，如果之前并没有存在这个实体
     */
    public E putIfAbsent(E entity) {
        ProcessContext processContext = ThreadBoundProcessContextArray.getProcessContext();
        if (processContext == null || !processContext.isStarted()) {
            throw new RuntimeException("can not put to repository without a process");
        }

        ID id = getId(entity);
        //先要看过程中的，如果有可用的那就拿来做实际值，如果有但是不可用那就取用新值且新值覆盖老值
        ProcessEntity<E> processEntity = processContext.getEntityInProcess(name, id);
        if (processEntity != null) {
            processEntity.changeStateByPutIfAbsent();
            if (processEntity.isAvailable()) {
                return processEntity.getEntity();
            } else {
                processEntity.setEntity(entity);
                return null;
            }
        }

        boolean ok = mutexes.newAndLock(id, processContext.getProcessName());
        if (!ok) {
            E exists = take(id);
            return exists;
        }
        store.insert(id, entity);
        processContext.addEntityCreatedAndTakenFromRepo(name, id, entity);
        return null;
    }

    /**
     * @param id
     * @param newEntity
     * @return 实际值
     */
    public E takeOrPutIfAbsent(ID id, E newEntity) {
        E entity = take(id);
        if (entity != null) {
            return entity;
        }
        E exists = putIfAbsent(newEntity);
        if (exists != null) {
            return exists;
        }
        return newEntity;
    }

    public E remove(ID id) {
        E entity = take(id);
        if (entity != null) {
            ProcessContext processContext = ThreadBoundProcessContextArray.getProcessContext();
            processContext.removeEntityInProcess(name, id);
        }
        return entity;
    }

    private void createEntityIdGetter(Class<?> entityClass) throws Exception {

        //取名称为“id”的field作为id field，如果不存在 “id” field，那么取第一个field作为id field
        Field idField = null;
        Field[] fields = entityClass.getDeclaredFields();
        for (Field field : fields) {
            if (field.getName().equals("id")) {
                idField = field;
                break;
            }
        }
        if (idField == null) {
            if (fields.length > 0) {
                idField = fields[0];
            } else {
                throw new RuntimeException("can not find id field in entity class " + entityClass.getName());
            }
        }


        entityIDField = idField.getName();
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

    public String getName() {
        return name;
    }

    public String getEntityIDField() {
        return entityIDField;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Class<E> getEntityType() {
        return entityType;
    }

    public Store<E, ID> getStore() {
        return store;
    }

    public void setStore(Store<E, ID> store) {
        this.store = store;
    }

    public Mutexes<ID> getMutexes() {
        return mutexes;
    }

    public void setMutexes(Mutexes<ID> mutexes) {
        this.mutexes = mutexes;
    }
}

interface EntityIdGetter {
    Object getId(Object entity);
}