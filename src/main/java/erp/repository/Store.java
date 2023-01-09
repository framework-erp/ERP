package erp.repository;

import erp.process.ProcessEntity;

import java.util.Map;
import java.util.Set;

public interface Store<E, ID> {

    /**
     * 加载上来的是原始entity的一个副本(copy)，基于数据库的store天然就是copy，而内存store需要实现copy而不能传递原始entity的ref
     */
    E load(ID id);

    void insert(ID id, E entity);

    void saveAll(Map<Object, Object> entitiesToInsert, Map<Object, ProcessEntity> entitiesToUpdate);

    void removeAll(Set<Object> ids);
}
