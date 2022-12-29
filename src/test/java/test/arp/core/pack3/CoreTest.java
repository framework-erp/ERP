package test.arp.core.pack3;

import arp.ARP;
import org.junit.Test;
import test.arp.core.F4Result;

import static org.junit.Assert.assertEquals;

public class CoreTest {

    @Test
    public void test() {
        TestService service = new TestService();

        ARP.go("f3", () -> {
            service.f3(2, 100);
            return null;
        });

        ARP.go("f3", () -> {
            service.f3(3, 100);
            return null;
        });
        F4Result f4Result1 = ARP.go("f4", () -> {
            return service.f4(2, 3, 50);
        });

        assertEquals(50, f4Result1.getEntity1().getiValue());
        assertEquals(150, f4Result1.getEntity2().getiValue());

    }

}
