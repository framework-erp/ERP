package arp.process;

import arp.repository.compare.EntityComparator;

public class ProcessEntity<T> {
	private T initialEntitySnapshot;
	private T entity;
	private ProcessEntityState state;

	public boolean changed() {
		return !EntityComparator.equals(initialEntitySnapshot, entity);
	}

	public void updateStateByTake() {
		state = state.take();
	}

	public void updateStateByPut() {
		state = state.put();
	}

	public void updateStateByRemove() {
		state = state.remove();
	}

	public ProcessEntityState getState() {
		return state;
	}

	public T getInitialEntitySnapshot() {
		return initialEntitySnapshot;
	}

	public void setInitialEntitySnapshot(T initialEntitySnapshot) {
		this.initialEntitySnapshot = initialEntitySnapshot;
	}

	public T getEntity() {
		return entity;
	}

	public void setEntity(T entity) {
		this.entity = entity;
	}

	public void setState(ProcessEntityState state) {
		this.state = state;
	}

}
