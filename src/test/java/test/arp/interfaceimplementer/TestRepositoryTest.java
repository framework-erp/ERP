package test.arp.interfaceimplementer;

import erp.ERP;
import erp.repository.impl.mem.MemRepository;
import erp.repository.interfaceimplementer.InterfaceRepositoryImplBuilder;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestRepositoryTest {

    @Test
    public void test() {
        TestEntityRepository<TestEntityImpl, String> testEntityRepository =
                InterfaceRepositoryImplBuilder.build(TestEntityRepository.class, new MemRepository<>(TestEntityImpl.class));
        TestEntityImpl testEntity0 = ERP.go("test", () -> {
            TestEntityImpl testEntity = new TestEntityImpl("0");
            testEntityRepository.put(testEntity);
            return testEntity;
        });
        TestEntityImpl testEntity1 = testEntityRepository.find(testEntity0.getId());
        assertEquals(testEntity1.getId(), testEntity0.getId());

        TestEntityImplRepository testEntityImplRepository =
                InterfaceRepositoryImplBuilder.build(TestEntityImplRepository.class, new MemRepository<>(TestEntityImpl.class));
        TestEntityImpl testEntityImpl0 = ERP.go("test", () -> {
            TestEntityImpl testEntityImpl = new TestEntityImpl("1");
            testEntityImplRepository.put(testEntityImpl);
            return testEntityImpl;
        });
        TestEntityImpl testEntityImpl1 = testEntityImplRepository.find(testEntityImpl0.getId());
        assertEquals(testEntityImpl1.getId(), testEntityImpl0.getId());

        TestEntityImplRepositoryExtendsCommon testEntityImplRepositoryExtendsCommon =
                InterfaceRepositoryImplBuilder.build(TestEntityImplRepositoryExtendsCommon.class, new MemRepository<>(TestEntityImpl.class));
        TestEntityImpl testEntityImpl2 = ERP.go("test", () -> {
            TestEntityImpl testEntityImpl = new TestEntityImpl("2");
            testEntityImplRepositoryExtendsCommon.put(testEntityImpl);
            return testEntityImpl;
        });
        TestEntityImpl testEntityImpl3 = testEntityImplRepositoryExtendsCommon.find(testEntityImpl2.getId());
        assertEquals(testEntityImpl3.getId(), testEntityImpl2.getId());

        TestEntityImplRepositoryExtendsSuper testEntityImplRepositoryExtendsSuper =
                InterfaceRepositoryImplBuilder.build(TestEntityImplRepositoryExtendsSuper.class, new MemRepository<>(TestEntityImpl.class));
        TestEntityImpl testEntityImpl4 = ERP.go("test", () -> {
            TestEntityImpl testEntityImpl = new TestEntityImpl("3");
            testEntityImplRepositoryExtendsSuper.put(testEntityImpl);
            return testEntityImpl;
        });
        TestEntityImpl testEntityImpl5 = testEntityImplRepositoryExtendsSuper.find(testEntityImpl4.getId());
        assertEquals(testEntityImpl5.getId(), testEntityImpl4.getId());


    }

}
