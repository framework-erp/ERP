package test.arp.core;

import arp.core.EntityCollectionRepository;

public class TestEntityRepository extends EntityCollectionRepository<Integer, TestEntity> {

	@Override
	protected Integer getId(TestEntity entity) {
		return entity.getId();
	}

}
