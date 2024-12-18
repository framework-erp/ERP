package erp.repository.impl.mem;

import erp.repository.SingletonEntity;
import erp.repository.SingletonRepository;

public class MemSingletonRepository<E> extends SingletonRepository<E> {

    private static final MemRepository<SingletonEntity, String> SINGLETON_CONTAINER =
            new MemRepository<>(SingletonEntity.class, "erp.repository.impl.mem.MemSingletonRepository");

    protected MemSingletonRepository() {
        this.singletonEntitiesContainer = SINGLETON_CONTAINER;
    }

    public MemSingletonRepository(String repositoryName) {
        super(repositoryName);
        this.singletonEntitiesContainer = SINGLETON_CONTAINER;
    }

    public MemSingletonRepository(E entity) {
        super(entity);
        this.singletonEntitiesContainer = SINGLETON_CONTAINER;
        SingletonEntity singletonEntity = new SingletonEntity();
        singletonEntity.setName(name);
        singletonEntity.setEntity(entity);
        this.singletonEntitiesContainer.getStore().insert(name, singletonEntity);
    }

    public MemSingletonRepository(E entity, String repositoryName) {
        super(repositoryName);
        this.singletonEntitiesContainer = SINGLETON_CONTAINER;
        SingletonEntity singletonEntity = new SingletonEntity();
        singletonEntity.setName(name);
        singletonEntity.setEntity(entity);
        this.singletonEntitiesContainer.getStore().insert(name, singletonEntity);
    }

}
