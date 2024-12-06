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
        if (repositories.containsKey(repository.getName())) {
            throw new RuntimeException("Repository already registered: " + repository.getName());
        }
        repositories.put(repository.getName(), new InnerRepository(repository));
    }

    /**
     * 注销Repository
     *
     * @param repository
     */
    public static void unregisterRepository(Repository repository) {
        repositories.remove(repository.getName());
    }

    public static InnerRepository getRepository(String repositoryName) {
        return repositories.get(repositoryName);
    }

    public static void registerSingletonRepository(SingletonRepository singletonRepository) {
        if (singletonRepositories.containsKey(singletonRepository.getName())) {
            throw new RuntimeException("SingletonRepository already registered: " + singletonRepository.getName());
        }
        singletonRepositories.put(singletonRepository.getName(), new InnerSingletonRepository(singletonRepository));
    }

    public static InnerSingletonRepository getSingletonRepository(String repositoryName) {
        return singletonRepositories.get(repositoryName);
    }
}
