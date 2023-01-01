package erp.repository;

public class PutIfAbsentResult<E> {
    private E actual;
    private boolean absent;

    public PutIfAbsentResult(E actual, boolean absent) {
        this.actual = actual;
        this.absent = absent;
    }

    public E getActual() {
        return actual;
    }

    public boolean isAbsent() {
        return absent;
    }
}
