package test.arp.core;

import arp.process.Process;

public class TestService {

	private TestEntityRepository testEntityRepository = new TestEntityRepository();

	@Process
	public TestEntity f1(int id) {
		TestEntity entity = new TestEntity();
		entity.setId(id);
		TestEntity existsEntity = testEntityRepository.saveIfAbsent(entity);
		if (existsEntity != null) {
			return existsEntity;
		} else {
			return entity;
		}
	}

	@Process
	public TestEntity f2(int id) {
		TestEntity entity = testEntityRepository.findByIdForUpdate(id);
		entity.setiValue(1);
		return entity;
	}

	@Process
	public void f3(int id, int iValue) {
		TestEntity entity = new TestEntity();
		entity.setId(id);
		entity.setiValue(iValue);
		testEntityRepository.save(entity);
	}

	@Process
	public F4Result f4(int id1, int id2, int value) {
		TestEntity entity1 = testEntityRepository.findByIdForUpdate(id1);
		entity1.setiValue(entity1.getiValue() - value);
		TestEntity entity2 = testEntityRepository.findByIdForUpdate(id2);
		entity2.setiValue(entity2.getiValue() + value);
		return new F4Result(entity1, entity2);
	}

}