package test.arp.core;

import arp.repository.MemRepository;

public class TestEntityRepository extends MemRepository<Integer, TestEntity> {

	@Override
	protected Integer getId(TestEntity entity) {
		return entity.getId();
	}

}
