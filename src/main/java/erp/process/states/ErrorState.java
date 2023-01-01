package erp.process.states;

/**
 * 错误状态
 */
public class ErrorState implements ProcessEntityState {
    @Override
    public ProcessEntityState transferByTake() {
        return this;
    }

    @Override
    public ProcessEntityState transferByPut() {
        return this;
    }

    @Override
    public ProcessEntityState transferByPutIfAbsent() {
        return this;
    }

    @Override
    public ProcessEntityState transferByRemove() {
        return this;
    }

    @Override
    public boolean isEntityAvailable() {
        return false;
    }

    @Override
    public boolean isAddByTake() {
        return false;
    }
}
