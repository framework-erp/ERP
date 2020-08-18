package arp.core;

import java.util.Map;
import java.util.Set;

public interface Store<I, T> {

	public T findForTake(I id);

	public T findForRead(I id);

	public void checkAndUpdateAll(Map<I, T> entities);

	public void saveAll(Map<I, T> entities);

	public void removeAll(Set<I> ids);

}
