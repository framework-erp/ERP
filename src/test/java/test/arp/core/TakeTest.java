package test.arp.core;

import arp.ARP;
import org.junit.Test;
import test.arp.core.pack1.TestService;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * @author zheng chengdong
 */
public class TakeTest {
    @Test
    public void test() throws java.lang.IllegalAccessException {
        ClassLoader currLoader = Thread.currentThread().getContextClassLoader();
        Class clazz = currLoader.getClass();
        Field parentField = null;
        while (clazz != null) {
            try {
                parentField = clazz.getDeclaredField("parent");
                parentField.setAccessible(true);
                break;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
                continue;
            }
        }
        Object parent = parentField.get(currLoader);


        try {
            ARP.start("test.arp.core.pack1", "test.arp.core.pack2");
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
