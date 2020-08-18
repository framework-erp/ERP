package arp.core;

public class ProcessEntity<T> {
	private T entity;
	private ProcessEntityState state;

	public void updateStateByTake() {
		state = state.take();
	}

	public void updateStateByPut() {
		state = state.put();
	}

	public ProcessEntityState getState() {
		return state;
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
