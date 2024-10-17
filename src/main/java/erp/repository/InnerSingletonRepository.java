package erp.repository;

/**
 * 对内的独立实体仓库操作集合
 */
public class InnerSingletonRepository {
    private SingletonRepository singletonRepository;

    public InnerSingletonRepository(SingletonRepository singletonRepository) {
        this.singletonRepository = singletonRepository;
    }

    public void releaseProcessEntity() {
        singletonRepository.releaseLock();
    }
}
