package erp.repository;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 保存的是一个不存在于某个集合当中的单例的实体。
 */
public abstract class SingletonRepository<T> {

    protected String name;

    protected Repository<SingletonEntity, String> singletonEntitiesContainer;

    protected SingletonRepository(String repositoryName) {
        this.name = repositoryName;
    }

    protected SingletonRepository(T entity) {
        this(entity.getClass().getName());
    }

    protected SingletonRepository() {
        Type genType = getClass().getGenericSuperclass();
        Type paramsType = ((ParameterizedType) genType).getActualTypeArguments()[0];
        name = paramsType.getTypeName();
    }


    public T get() {
        SingletonEntity singletonEntity = singletonEntitiesContainer.find(name);
        if (singletonEntity == null) {
            return null;
        }
        return (T) singletonEntity.getEntity();
    }

    public T take() {
        SingletonEntity singletonEntity = singletonEntitiesContainer.take(name);
        if (singletonEntity == null) {
            return null;
        }
        return (T) singletonEntity.getEntity();
    }

    public void put(T entity) {
        SingletonEntity singletonEntity = new SingletonEntity();
        singletonEntity.setName(name);
        singletonEntity.setEntity(entity);
        singletonEntitiesContainer.putIfAbsent(singletonEntity);
    }

    public T putIfAbsent(T entity) {
        SingletonEntity singletonEntity = new SingletonEntity();
        singletonEntity.setName(name);
        singletonEntity.setEntity(entity);
        SingletonEntity existingEntity = singletonEntitiesContainer.putIfAbsent(singletonEntity);
        if (existingEntity == null) {
            return null;
        }
        return (T) existingEntity.getEntity();
    }

    public T takeOrPutIfAbsent(T newEntity) {
        SingletonEntity singletonEntity = new SingletonEntity();
        singletonEntity.setName(name);
        singletonEntity.setEntity(newEntity);
        SingletonEntity existingEntity = singletonEntitiesContainer.takeOrPutIfAbsent(name, singletonEntity);
        return (T) existingEntity.getEntity();
    }

    public T remove() {
        SingletonEntity singletonEntity = singletonEntitiesContainer.remove(name);
        if (singletonEntity == null) {
            return null;
        }
        return (T) singletonEntity.getEntity();
    }

    public String getName() {
        return name;
    }
}
