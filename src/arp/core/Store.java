package arp.core;

import java.util.Map;
import java.util.Set;

public interface Store<ID, T> {

	public T findForTake(ID id);

	public T findForRead(ID id);

	// 有返回，相当于随后findForTake
	public T createIfAbsent(ID id, T entity);

	public void checkAndUpdateAll(Map<ID, T> entities);

	public void saveAll(Map<ID, T> entities);

	public void removeAll(Set<ID> ids);

}
