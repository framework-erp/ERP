package erp.repository.interfaceimplementer;

public interface TemplateEntityRepository {
    TemplateEntityImpl take(Object id);

    TemplateEntityImpl find(Object id);

    void put(TemplateEntityImpl entity);

    TemplateEntityImpl putIfAbsent(TemplateEntityImpl entity);

    TemplateEntityImpl takeOrPutIfAbsent(Object id, TemplateEntityImpl newEntity);

    TemplateEntityImpl remove(Object id);
}
