package test.arp.core;

import static org.junit.Assert.assertEquals;



import org.junit.Test;

import arp.enhance.ClassEnhancer;

public class CoreTest {

	@Test
	public void test() {
		try {
			ClassEnhancer.parseAndEnhance("test.arp.core");
		} catch (Exception e) {
			e.printStackTrace();
		}

		TestService service = new TestService();
		TestEntity entity1 = service.f1(1);
		assertEquals(1, entity1.getId());
		entity1.setiValue(2);
		assertEquals(2, entity1.getiValue());

		TestEntity entity2 = service.f2(1);
		assertEquals(1, entity2.getId());
		assertEquals(1, entity2.getiValue());

		TestEntity entity3 = service.f1(1);
		assertEquals(1, entity3.getId());
		assertEquals(1, entity3.getiValue());

		service.f3(2, 100);
		service.f3(3, 100);

		F4Result f4Result1 = service.f4(2, 3, 50);
		assertEquals(50, f4Result1.getEntity1().getiValue());
		assertEquals(150, f4Result1.getEntity2().getiValue());

	}

}
