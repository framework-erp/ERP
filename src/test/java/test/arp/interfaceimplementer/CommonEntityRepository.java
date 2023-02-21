package test.arp.interfaceimplementer;

interface CommonEntityRepository<E, ID> {
    E take(ID var1);

    E find(ID var1);

    void put(E var1);

    E putIfAbsent(E var1);

    E takeOrPutIfAbsent(ID var1, E var2);

    E remove(ID var1);
}