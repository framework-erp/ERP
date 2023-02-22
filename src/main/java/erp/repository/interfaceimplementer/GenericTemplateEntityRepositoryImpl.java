package erp.repository.interfaceimplementer;

import erp.repository.Repository;

public class GenericTemplateEntityRepositoryImpl implements GenericTemplateEntityRepository<TemplateEntityImpl, Integer> {

    private Repository underlyingRepository;

    public GenericTemplateEntityRepositoryImpl(Repository<?, ?> underlyingRepository) {
        this.underlyingRepository = underlyingRepository;
    }

    @Override
    public TemplateEntityImpl take(Integer id) {
        return (TemplateEntityImpl) underlyingRepository.take(id);
    }

    @Override
    public TemplateEntityImpl find(Integer id) {
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
    public TemplateEntityImpl takeOrPutIfAbsent(Integer id, TemplateEntityImpl newEntity) {
        return (TemplateEntityImpl) underlyingRepository.takeOrPutIfAbsent(id, newEntity);
    }

    @Override
    public TemplateEntityImpl remove(Integer id) {
        return (TemplateEntityImpl) underlyingRepository.remove(id);
    }
}
