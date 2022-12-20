package test.arp.core;

import arp.ARP;
import org.junit.Test;
import test.arp.core.pack1.TestService;

/**
 * @author zheng chengdong
 */
public class TakeTest {
    @Test
    public void test() throws java.lang.IllegalAccessException {
        ARP.useAnnotation();
        TestService service = new TestService();
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
