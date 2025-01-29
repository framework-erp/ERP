package erp;

import erp.process.ProcessListener;
import erp.repository.InnerRepository;
import erp.repository.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AppContext {
    private static Map<String, InnerRepository> repositories = new ConcurrentHashMap<>();
    private static List<ProcessListener> processListeners = new ArrayList<>();

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

    public static void registerProcessListener(ProcessListener newProcessListener) {
        for (ProcessListener processListener : processListeners) {
            if (processListener.getClass().equals(newProcessListener.getClass())) {
                return;
            }
        }
        processListeners.add(newProcessListener);
    }

    public static <T> T getProcessListener(Class<T> clazz) {
        for (ProcessListener processListener : processListeners) {
            if (clazz.isInstance(processListener)) {
                return (T) processListener;
            }
        }
        return null;
    }

    public static List<ProcessListener> getProcessListeners() {
        return processListeners;
    }

}
