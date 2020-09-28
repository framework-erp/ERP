package test.arp.core;

import java.util.Map;
import java.util.Set;

import arp.core.Repository;

public class TestEntityRepository extends Repository<Integer, TestEntity> {

	public TestEntityRepository() {
		super(true);
	}

	@Override
	protected Integer getId(TestEntity entity) {
		return entity.getId();
	}

	@Override
	protected TestEntity doFindByIdForUpdate(Integer id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected TestEntity doFindById(Integer id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected TestEntity doSaveIfAbsent(Integer id, TestEntity entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void removeAll(Set<Integer> ids) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void updateAll(Map<Integer, TestEntity> entities) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void saveAll(Map<Integer, TestEntity> entities) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void unlockAll(Set<Integer> ids) {
		// TODO Auto-generated method stub

	}

}
