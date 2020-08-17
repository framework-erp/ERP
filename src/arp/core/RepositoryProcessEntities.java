package arp.core;

import java.util.HashMap;
import java.util.Map;

public class RepositoryProcessEntities<ID, T> {

	private int repositoryId;

	private Map<ID, T> getEntities = new HashMap<>();

	private Map<ID, T> putEntities = new HashMap<>();

	private Map<ID, T> removeEntities = new HashMap<>();

	public RepositoryProcessEntities(int repositoryId) {
		this.repositoryId = repositoryId;
	}

	public void putEntityForGet(ID id, T entity) {
		getEntities.put(id, entity);
	}

	public void putEntityForGetForRead(ID id, T entity) {
		getForReadEntities.put(id, entity);
	}

	public void putEntityForPut(ID id, T entity) {
		putEntities.put(id, entity);
	}

	public void putEntityForRemove(ID id, T entity) {
		removeEntities.put(id, entity);
	}

	public boolean hasRemovedEntity(ID id) {
		return removeEntities.containsKey(id);
	}

	public void removeEntity(ID id) {
		T entity = putEntities.remove(id);
		if (entity == null) {
			entity = getEntities.remove(id);
		}
		getForReadEntities.remove(id);
		if (entity != null) {
			removeEntities.put(id, entity);
		}
	}

	public Map<ID, T> getGetEntities() {
		return getEntities;
	}

	public Map<ID, T> getPutEntities() {
		return putEntities;
	}

	public Map<ID, T> getRemoveEntities() {
		return removeEntities;
	}

	public int getRepositoryId() {
		return repositoryId;
	}

}
