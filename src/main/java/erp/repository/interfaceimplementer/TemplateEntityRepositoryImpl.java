package erp.repository.interfaceimplementer;

import erp.repository.Repository;

public class TemplateEntityRepositoryImpl implements TemplateEntityRepository {

    private Repository underlyingRepository;

    public TemplateEntityRepositoryImpl(Repository<?, ?> underlyingRepository) {
        this.underlyingRepository = underlyingRepository;
    }

    @Override
    public TemplateEntityImpl take(Object id) {
        return (TemplateEntityImpl) underlyingRepository.take(id);
    }

    @Override
    public TemplateEntityImpl find(Object id) {
        return (TemplateEntityImpl) underlyingRepository.find(id);
    }

    @Override
    public void put(TemplateEntityImpl entity) {
        underlyingRepository.put(entity);
    }

    @Override
    public TemplateEntityImpl putIfAbsent(TemplateEntityImpl entity) {
        return (TemplateEntityImpl) underlyingRepository.putIfAbsent(entity);
    }

    @Override
    public TemplateEntityImpl takeOrPutIfAbsent(Object id, TemplateEntityImpl newEntity) {
        return (TemplateEntityImpl) underlyingRepository.takeOrPutIfAbsent(id, newEntity);
    }

    @Override
    public TemplateEntityImpl remove(Object id) {
        return (TemplateEntityImpl) underlyingRepository.remove(id);
    }
}
