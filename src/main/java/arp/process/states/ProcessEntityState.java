package arp.process.states;

/**
 * 过程当中的实体的可能的几种状态
 */
public interface ProcessEntityState {
    ProcessEntityState transferByTake();

    ProcessEntityState transferByPut();

    ProcessEntityState transferByPutIfAbsent();

    ProcessEntityState transferByRemove();

    boolean isEntityAvailable();

    boolean isAddByTake();
}
