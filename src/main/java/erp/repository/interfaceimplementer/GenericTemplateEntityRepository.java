package erp.repository.interfaceimplementer;

public interface GenericTemplateEntityRepository<T extends TemplateEntity, ID> {
    T take(ID id);

    T find(ID id);

    void put(T entity);

    T putIfAbsent(T entity);

    T takeOrPutIfAbsent(ID id, T newEntity);

    T remove(ID id);
}
