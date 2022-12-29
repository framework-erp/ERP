package test.arp.core.pack3;

import arp.annotation.Process;
import arp.repository.PutIfAbsentResult;
import arp.repository.TakeEntityException;
import test.arp.core.F4Result;
import test.arp.core.TestEntity;
import test.arp.core.TestEntityRepository;

import java.util.concurrent.locks.LockSupport;

public class TestService {

    private TestEntityRepository testEntityRepository = new TestEntityRepository();

    public void f3(int id, int iValue) {
        TestEntity entity = new TestEntity();
        entity.setId(id);
        entity.setiValue(iValue);
        testEntityRepository.put(entity);
    }

    public F4Result f4(int id1, int id2, int value) throws Exception {
        TestEntity entity1 = testEntityRepository.take(id1);
        entity1.setiValue(entity1.getiValue() - value);
        TestEntity entity2 = testEntityRepository.take(id2);
        entity2.setiValue(entity2.getiValue() + value);
        return new F4Result(entity1, entity2);
    }


}
