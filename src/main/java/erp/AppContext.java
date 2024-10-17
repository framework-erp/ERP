package erp;

import erp.repository.InnerRepository;
import erp.repository.InnerSingletonRepository;
import erp.repository.Repository;
import erp.repository.SingletonRepository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AppContext {
    private static Map<String, InnerRepository> repositories = new ConcurrentHashMap<>();
    private static Map<String, InnerSingletonRepository> singletonRepositories = new ConcurrentHashMap<>();

    public static void registerRepository(Repository repository) {
        repositories.put(repository.getEntityType(), new InnerRepository(repository));
    }

    public static InnerRepository getRepository(String entityType) {
        return repositories.get(entityType);
    }

    public static void registerSingletonRepository(String entityType, SingletonRepository singletonRepository) {
        singletonRepositories.put(entityType, new InnerSingletonRepository(singletonRepository));
    }

    public static InnerSingletonRepository getSingletonRepository(String entityType) {
        return singletonRepositories.get(entityType);
    }
}
