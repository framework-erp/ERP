package erp.repository.interfaceimplementer;

interface TemplateEntityImplRepository {
    TemplateEntityImpl take(Integer id);

    TemplateEntityImpl find(Integer id);

    void put(TemplateEntityImpl entity);

    TemplateEntityImpl putIfAbsent(TemplateEntityImpl entity);

    TemplateEntityImpl takeOrPutIfAbsent(Integer id, TemplateEntityImpl newEntity);

    TemplateEntityImpl remove(Integer id);
}
