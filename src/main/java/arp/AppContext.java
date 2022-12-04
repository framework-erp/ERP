package arp;

import arp.repository.InnerRepository;
import arp.repository.Mutexes;
import arp.repository.Repository;
import arp.repository.Store;

import java.util.HashMap;
import java.util.Map;

public class AppContext {
    private static Map<String, InnerRepository> repositories = new HashMap<>();

    public static void registerRepository(String aggType, Store store, Mutexes mutexes) {
        repositories.put(aggType, new InnerRepository(store, mutexes));
    }

    public static InnerRepository getRepository(String aggType) {
        return repositories.get(aggType);
    }
}
