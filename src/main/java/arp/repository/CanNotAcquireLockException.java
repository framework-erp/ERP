package arp.repository;

public class CanNotAcquireLockException extends RuntimeException {
	private static final long serialVersionUID = 4650998033057407283L;

	public CanNotAcquireLockException(String nameOfProcessOccupyingLock) {
		super("lock is occupied by process: " + nameOfProcessOccupyingLock);
	}
}
