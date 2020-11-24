package arp.process;

public class TransientProcessEntityState implements ProcessEntityState {

	@Override
	public ProcessEntityState take() {
		return this;
	}

	@Override
	public ProcessEntityState put() {
		return new CreatedProcessEntityState();
	}

	@Override
	public ProcessEntityState remove() {
		return this;
	}

}
