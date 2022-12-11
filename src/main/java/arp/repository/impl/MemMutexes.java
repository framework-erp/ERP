package arp.repository.impl;

import arp.repository.Mutexes;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class MemMutexes<ID> implements Mutexes<ID> {

    private Map<ID, AtomicInteger> locks = new ConcurrentHashMap<>();

    private Map<ID, String> lockProcessNames = new ConcurrentHashMap<>();

    @Override
    public boolean exists(ID id) {
        return locks.containsKey(id);
    }

    @Override
    public int lock(ID id, String processName) {
        if (!locks.containsKey(id)) {
            return -1;
        }
        AtomicInteger lock = locks.get(id);
        if (lock.compareAndSet(0, 1)) {
            lockProcessNames.put(id, processName);
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public boolean newAndLock(ID id) {
        AtomicInteger lock = new AtomicInteger(1);
        AtomicInteger loaded = locks.putIfAbsent(id, lock);
        return loaded == null;
    }

    @Override
    public void unlockAll(Set<Object> ids) {
        for (Object id : ids) {
            AtomicInteger lock = locks.get(id);
            if (lock != null) {
                lock.set(0);
            }
        }
    }

    @Override
    public String getLockProcess(ID id) {
        return lockProcessNames.get(id);
    }

    @Override
    public void removeAll(Set<Object> ids) {
        for (Object id : ids) {
            locks.remove(id);
        }
    }
}
