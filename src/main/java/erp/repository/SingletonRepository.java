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
        return (T) singletonEntitiesContainer.find(name).getEntity();
    }

    public T take() {
        return (T) singletonEntitiesContainer.take(name).getEntity();
    }

    public void put(T entity) {
        SingletonEntity singletonEntity = new SingletonEntity();
        singletonEntity.setName(name);
        singletonEntity.setEntity(entity);
        singletonEntitiesContainer.putIfAbsent(singletonEntity);
    }

    public String getName() {
        return name;
    }
}
