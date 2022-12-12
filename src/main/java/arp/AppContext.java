package arp;

import arp.repository.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class AppContext {
    private static Map<String, InnerRepository> repositories = new HashMap<>();
    private static Map<String, InnerSingletonRepository> singletonRepositories = new HashMap<>();

    public static void registerRepository(String aggType, Store store, Mutexes mutexes) {
        repositories.put(aggType, new InnerRepository(store, mutexes));
    }

    public static InnerRepository getRepository(String aggType) {
        return repositories.get(aggType);
    }

    public static void registerSingletonRepository(String aggType, AtomicInteger lock) {
        singletonRepositories.put(aggType, new InnerSingletonRepository(lock));
    }

    public static InnerSingletonRepository getSingletonRepository(String aggType) {
        return singletonRepositories.get(aggType);
    }
}
