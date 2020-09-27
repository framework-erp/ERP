package test.arp.core;

import arp.core.Repository;

public class TestEntityRepository extends Repository<Integer, TestEntity> {

	@Override
	protected Integer getId(TestEntity entity) {
		return entity.getId();
	}

}
