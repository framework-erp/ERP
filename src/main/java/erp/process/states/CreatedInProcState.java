package erp.process.states;

/**
 * 在过程中新建的状态
 */
public class CreatedInProcState implements ProcessEntityState {
    @Override
    public ProcessEntityState transferByTake() {
        return this;
    }

    @Override
    public ProcessEntityState transferByPut() {
        return new ErrorState();
    }

    @Override
    public ProcessEntityState transferByPutIfAbsent() {
        return this;
    }

    @Override
    public ProcessEntityState transferByRemove() {
        return new TransientInProcState();
    }

    @Override
    public boolean isEntityAvailable() {
        return true;
    }

    @Override
    public boolean isAddByTake() {
        return false;
    }
}
