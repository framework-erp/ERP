package test.arp.core;

import erp.ERP;
import org.junit.Test;
import test.arp.core.pack1.TestService;

/**
 * @author zheng chengdong
 */
public class TakeTest {
    @Test
    public void test() throws java.lang.IllegalAccessException {
        ERP.useAnnotation();
        TestEntityRepository testEntityRepository = new TestEntityRepository();
        TestService service = new TestService();
        service.setTestEntityRepository(testEntityRepository);
        TestEntity entity1 = service.f1(1);
        Thread t1 = new Thread(new TakeAndWaitWorker(service));
        Thread t2 = new Thread(new TakeAndWaitWorker(service));
        Thread t3 = new Thread(new TakeAndWaitWorker(service));
        t1.start();
        t2.start();
        t3.start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        TestEntity entity3 = service.f2(1);
    }
}
