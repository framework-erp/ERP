package arp.process;

public class RemovedProcessEntityState implements ProcessEntityState {

	@Override
	public ProcessEntityState take() {
		return this;
	}

	@Override
	public ProcessEntityState put() {
		return new TakenProcessEntityState();
	}

	@Override
	public ProcessEntityState remove() {
		return this;
	}

}
