package arp.process.states;

/**
 * 需要去仓库中删除的状态
 */
public class ToRemoveInRepoState implements ProcessEntityState{
    @Override
    public ProcessEntityState transferByTake() {
        return this;
    }

    @Override
    public ProcessEntityState transferByPut() {
        return new TakenFromRepoState();
    }

    @Override
    public ProcessEntityState transferByPutIfAbsent() {
        return new TakenFromRepoState();
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
        return true;
    }
}
