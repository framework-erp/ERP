package arp.core;

public interface ProcessEntityState {
	public ProcessEntityState take();

	public ProcessEntityState put();

	public ProcessEntityState putIfAbsent();

	public ProcessEntityState remove();
}
