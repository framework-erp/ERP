package arp.repository;

import java.util.List;
import java.util.Set;

public interface Mutexes<ID> {
    boolean exists(ID id);

    /**
     * -1:锁不存在 0:锁失败 1:锁成功
     */
    int lock(ID id ) ;

    /**
     * 返回false那就是已创建了
     */
    boolean newAndLock(ID id);
    void unlockAll(Set<Object> ids );

    String getLockProcess();
}
