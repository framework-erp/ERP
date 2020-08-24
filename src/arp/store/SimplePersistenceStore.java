package arp.store;

import java.util.Map;
import java.util.Set;

public class SimplePersistenceStore<ID, T> extends PersistenceStore<ID, T> {

	@Override
	public T findForRead(ID id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected T findAndLock(ID id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected T createIfAbsentAndLock(ID id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void updateAndUnlockBatch(Map<ID, T> entitiesToUpdate) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void updateAndUnlock(ID id, T entity) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void createBatch(Map<ID, T> entities) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void create(ID id, T entity) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void removeBatch(Set<ID> ids) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void remove(ID id) {
		// TODO Auto-generated method stub

	}

}
