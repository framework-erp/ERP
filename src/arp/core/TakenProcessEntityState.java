package arp.core;

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
	public ProcessEntityState putIfAbsent() {
		return this;
	}

	@Override
	public ProcessEntityState remove() {
		// TODO Auto-generated method stub
		return null;
	}

}
