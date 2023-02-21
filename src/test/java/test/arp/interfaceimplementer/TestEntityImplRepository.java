package test.arp.interfaceimplementer;

public interface TestEntityImplRepository {
    TestEntityImpl take(String id);

    TestEntityImpl find(String id);

    void put(TestEntityImpl entity);

    TestEntityImpl putIfAbsent(TestEntityImpl entity);

    TestEntityImpl takeOrPutIfAbsent(String id, TestEntityImpl newEntity);

    TestEntityImpl remove(String id);
}
