package erp.repository.impl;

import erp.repository.Mutexes;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MemMutexes<ID> implements Mutexes<ID> {

    private Map<ID, MemMutex> mutexes = new ConcurrentHashMap<>();

    @Override
    public boolean exists(ID id) {
        return mutexes.containsKey(id);
    }

    @Override
    public int lock(ID id, String processName) {
        if (!mutexes.containsKey(id)) {
            return -1;
        }
        MemMutex mutex = mutexes.get(id);
        if (mutex.lock(processName)) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public boolean newAndLock(ID id, String processName) {
        MemMutex mutex = new MemMutex(processName);
        MemMutex loaded = mutexes.putIfAbsent(id, mutex);
        return loaded == null;
    }

    @Override
    public void unlockAll(Set<Object> ids) {
        for (Object id : ids) {
            MemMutex mutex = mutexes.get(id);
            if (mutex != null) {
                mutex.unlock();
            }
        }
    }

    @Override
    public String getLockProcess(ID id) {
        return mutexes.get(id).getLockProcess();
    }

    @Override
    public void removeAll(Set<Object> ids) {
        for (Object id : ids) {
            mutexes.remove(id);
        }
    }
}
