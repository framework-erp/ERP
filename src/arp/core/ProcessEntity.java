package arp.core;

public abstract class ProcessEntity<T, ID> {
	private T entity;

	public abstract ProcessEntity<T, ID> take();

	public abstract ProcessEntity<T, ID> put(T entity);

	public abstract ProcessEntity<T, ID> putIfAbsent(T entity);

	public abstract ProcessEntity<T, ID> remove();

}
