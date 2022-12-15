package test.arp.core.pack1;

import arp.repository.TakeEntityException;
import arp.repository.PutIfAbsentResult;
import test.arp.core.F4Result;
import test.arp.core.TestEntity;
import test.arp.core.TestEntityRepository;
import arp.annotation.Process;

import java.util.concurrent.locks.LockSupport;

public class TestService {

    private TestEntityRepository testEntityRepository = new TestEntityRepository();

    @Process
    public TestEntity f1(int id) {
        TestEntity entity = new TestEntity();
        entity.setId(id);
        PutIfAbsentResult<TestEntity> rslt = testEntityRepository.putIfAbsent(entity);
        return rslt.getActual();
    }

    @Process
    public TestEntity f2(int id) {
        TestEntity entity = testEntityRepository.take(id);
        entity.setiValue(1);
        return entity;
    }

    @Process
    public TestEntity takeAndWait(int id, long waitMs) {
        TestEntity entity = testEntityRepository.take(id);
        entity.setiValue(1);
        try {
            Thread.sleep(waitMs);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return entity;
    }

    @Process
    public void f3(int id, int iValue) {
        TestEntity entity = new TestEntity();
        entity.setId(id);
        entity.setiValue(iValue);
        testEntityRepository.put(entity);
    }

    public F4Result f4(int id1, int id2, int value) {
        int counter = 300;
        do {
            try {
                return df4(id1, id2, value);
            } catch (TakeEntityException e) {
                if (counter > 200) {
                    --counter;
                } else if (counter > 100) {
                    --counter;
                    Thread.yield();
                } else if (counter > 0) {
                    --counter;
                    LockSupport.parkNanos(1L);
                } else {
                    throw e;
                }
            }
        } while (true);
    }

    @Process
    public F4Result df4(int id1, int id2, int value) {
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
