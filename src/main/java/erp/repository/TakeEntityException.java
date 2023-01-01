package erp.repository;

public class TakeEntityException extends RuntimeException {
    private static final long serialVersionUID = 4650998033057407283L;

    public TakeEntityException(String nameOfProcessOccupyingLock) {
        super("entity is taken by process: " + nameOfProcessOccupyingLock);
    }
}
