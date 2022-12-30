package arp;

import arp.repository.InnerRepository;
import arp.repository.InnerSingletonRepository;
import arp.repository.Mutexes;
import arp.repository.Store;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class AppContext {
    private static Map<String, InnerRepository> repositories = new HashMap<>();
    private static Map<String, InnerSingletonRepository> singletonRepositories = new HashMap<>();

    public static void registerRepository(String entityType, Store store, Mutexes mutexes) {
        repositories.put(entityType, new InnerRepository(store, mutexes));
    }

    public static InnerRepository getRepository(String entityType) {
        return repositories.get(entityType);
    }

    public static void registerSingletonRepository(String entityType, AtomicInteger lock) {
        singletonRepositories.put(entityType, new InnerSingletonRepository(lock));
    }

    public static InnerSingletonRepository getSingletonRepository(String entityType) {
        return singletonRepositories.get(entityType);
    }
}
