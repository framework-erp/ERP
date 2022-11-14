package arp.process.states;

/**
 * 瞬时状态，就是在过程中创建之后又在过程中删除，和仓库没有关系
 */
public class TransientInProcState implements ProcessEntityState{
    @Override
    public ProcessEntityState transferByTake() {
        return new ErrorState();
    }

    @Override
    public ProcessEntityState transferByPut() {
        return new CreatedInProcState();
    }

    @Override
    public ProcessEntityState transferByPutIfAbsent() {
        return new CreatedInProcState();
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
