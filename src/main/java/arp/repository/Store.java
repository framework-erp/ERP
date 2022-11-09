package arp.repository;

import arp.process.ProcessEntity;

import java.util.List;
import java.util.Map;

public interface Store<E, ID> {

    /**
     * 加载上来的是原始entity的一个副本(copy)，基于数据库的store天然就是copy，而内存store需要实现copy而不能传递原始entity的ref
     */
    E load(ID id);

    void save(ID id, E entity);

    void saveAll(Map<ID, E> entitiesToInsert, Map<ID, ProcessEntity> entitiesToUpdate);

    void removeAll(List<ID> ids);
}
