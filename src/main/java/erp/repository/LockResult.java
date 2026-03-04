package erp.repository;

public class LockResult {
    public static final int SUCCESS = 1;
    public static final int FAILED = 0;
    public static final int NOT_FOUND = -1;

    public final int status;
    public final String owner;

    private LockResult(int status, String owner) {
        this.status = status;
        this.owner = owner;
    }

    public static LockResult success() {
        return new LockResult(SUCCESS, null);
    }

    public static LockResult failed(String owner) {
        return new LockResult(FAILED, owner);
    }

    public static LockResult notFound() {
        return new LockResult(NOT_FOUND, null);
    }
}
