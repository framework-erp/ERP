package erp.process.states;

/**
 * 从仓库中取来的状态
 */
public class TakenFromRepoState implements ProcessEntityState {
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
        return new ToRemoveInRepoState();
    }

    @Override
    public boolean isEntityAvailable() {
        return true;
    }

    @Override
    public boolean isAddByTake() {
        return true;
    }
}
