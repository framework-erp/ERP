package arp.repository;

import java.util.List;

public interface Mutexes<ID> {
    boolean exists(ID id);
    boolean lock(ID id ) ;

    /**
     * 返回false那就是已创建了
     */
    boolean newAndLock(ID id);
    void unlockAll(List<ID> ids );
}
