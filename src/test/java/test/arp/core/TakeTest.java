package test.arp.core;

import arp.ARP;
import arp.enhance.ProcessesClassLoader;
import arp.repository.TakeEntityException;
import org.junit.Test;
import test.arp.core.pack1.TestService;

import java.lang.reflect.Field;

/**
 * @author zheng chengdong
 */
public class TakeTest {
    @Test
    public void test() throws java.lang.IllegalAccessException {
        ARP.useAnnotation();
        try {
//            ARP.start("test.arp.core.pack1", "test.arp.core.pack2");
        } catch (Exception e) {
            e.printStackTrace();
        }
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
