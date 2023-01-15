package test.arp.core.pack2;

import erp.annotation.Process;
import test.arp.core.F4Result;
import test.arp.core.TestEntity;
import test.arp.core.TestEntityRepository;

public class TestService2 {

    private TestEntityRepository testEntityRepository = new TestEntityRepository();

    @Process
    public TestEntity f1(int id) {
        TestEntity entity = new TestEntity();
        entity.setId(id);
        TestEntity exists = testEntityRepository.putIfAbsent(entity);
        if (exists != null) {
            return exists;
        }
        return entity;
    }

    @Process
    public TestEntity f2(int id) {
        TestEntity entity = testEntityRepository.take(id);
        entity.setiValue(1);
        return entity;
    }

    @Process
    public void f3(int id, int iValue) {
        TestEntity entity = new TestEntity();
        entity.setId(id);
        entity.setiValue(iValue);
        testEntityRepository.put(entity);
    }

    @Process
    public F4Result f4(int id1, int id2, int value) {
        TestEntity entity1 = testEntityRepository.take(id1);
        entity1.setiValue(entity1.getiValue() - value);
        TestEntity entity2 = testEntityRepository.take(id2);
        entity2.setiValue(entity2.getiValue() + value);
        return new F4Result(entity1, entity2);
    }

    @Process
    public void f5(long l1, int l2) {
    }

}
