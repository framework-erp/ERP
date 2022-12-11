package arp.repository.impl;

import arp.repository.Repository;

public class MemRepository<E,ID> extends Repository<E,ID> {
    public MemRepository() {
        super(new MemStore<>(), new MemMutexes<>());
    }
}
