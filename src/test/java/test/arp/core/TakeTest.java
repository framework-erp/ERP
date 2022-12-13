package test.arp.core;

import arp.ARP;
import org.junit.Test;
import test.arp.core.pack1.TestService;

/**
 * @author zheng chengdong
 */
public class TakeTest {
    @Test
    public void test() {
        try {
            ARP.start("test.arp.core.pack1", "test.arp.core.pack2");
        } catch (Exception e) {
            e.printStackTrace();
        }

        TestService service = new TestService();
        TestEntity entity1 = service.f1(1);
        Thread t = new Thread(new TakeAndWaitWorker(service));
        t.start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        TestEntity entity3 = service.f2(1);
    }
}
