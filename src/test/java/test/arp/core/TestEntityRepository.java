package test.arp.core;

import arp.repository.MemRepository;

public class TestEntityRepository extends MemRepository<TestEntity, Integer> {

	@Override
	protected Integer getId(TestEntity entity) {
		return entity.getId();
	}

}
