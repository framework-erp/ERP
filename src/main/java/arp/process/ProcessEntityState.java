package arp.process;

public interface ProcessEntityState {
	public ProcessEntityState take();

	public ProcessEntityState put();

	public ProcessEntityState remove();
}
