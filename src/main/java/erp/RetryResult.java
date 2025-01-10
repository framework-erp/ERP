package erp;

public class RetryResult<T> {
    private T processReturnValue;
    private Exception exception;
    private int triedTimes;

    public T getReturn() {
        if (exception != null) {
            throw new RuntimeException(exception);
        }
        return processReturnValue;
    }

    void setProcessReturnValue(T processReturnValue) {
        this.processReturnValue = processReturnValue;
    }

    public Exception getException() {
        return exception;
    }

    void setException(Exception exception) {
        this.exception = exception;
    }

    public int getTriedTimes() {
        return triedTimes;
    }

    void setTriedTimes(int triedTimes) {
        this.triedTimes = triedTimes;
    }
}
