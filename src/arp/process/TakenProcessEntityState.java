package arp.process;

public class TakenProcessEntityState implements ProcessEntityState {

	@Override
	public ProcessEntityState take() {
		return this;
	}

	@Override
	public ProcessEntityState put() {
		return this;
	}

	@Override
	public ProcessEntityState remove() {
		return new RemovedProcessEntityState();
	}

}
