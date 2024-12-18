package erp;

import erp.repository.InnerRepository;
import erp.repository.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AppContext {
    private static Map<String, InnerRepository> repositories = new ConcurrentHashMap<>();

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

}
