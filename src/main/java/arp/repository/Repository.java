package arp.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import arp.process.CreatedProcessEntityState;
import arp.process.ProcessContext;
import arp.process.ProcessEntity;
import arp.process.ProcessEntityState;
import arp.process.TakenProcessEntityState;
import arp.process.ThreadBoundProcessContextArray;
import arp.process.TransientProcessEntityState;

public abstract class Repository<E, I> {

	private static AtomicInteger ids = new AtomicInteger();

	private static Repository[] repositories = new Repository[1024];

	private int id;

	private Map<I, E> mockStore;

	private boolean mock = false;

	public static Repository getRepository(int id) {
		return repositories[id];
	}

	protected Repository() {
		id = ids.incrementAndGet();
		repositories[id] = this;
	}

	protected void initAsMock() {
		this.mock = true;
		mockStore = new HashMap<>();
	}

	protected abstract I getId(E entity);

	public E findByIdForUpdate(I id) {
		ProcessContext processContext = ThreadBoundProcessContextArray
				.getProcessContext();
		if (!processContext.isStarted()) {
			throw new RuntimeException(
					"can not use repository without a process");
		}

		ProcessEntity<E> processEntity = processContext
				.getEntityInProcessForTake(this.id, id);
		if (processEntity != null) {
			ProcessEntityState entityState = processEntity.getState();
			if (entityState instanceof CreatedProcessEntityState
					|| entityState instanceof TakenProcessEntityState) {
				return processEntity.getEntity();
			} else {
				return null;
			}
		}

		E entity = doFindByIdForUpdate(id);
		if (entity != null) {
			processContext.takeEntityFromRepoAndPutInProcess(this.id, id,
					entity);
		}
		return entity;
	}

	private E doFindByIdForUpdate(I id) {
		if (!mock) {
			return findByIdForUpdateFromStore(id);
		} else {
			return mockStore.get(id);
		}
	}

	protected abstract E findByIdForUpdateFromStore(I id);

	public E findById(I id) {
		ProcessContext processContext = ThreadBoundProcessContextArray
				.getProcessContext();
		if (!processContext.isStarted()) {
			throw new RuntimeException(
					"can not use repository without a process");
		}
		E entity = processContext.copyEntityInProcess(this.id, id);
		if (entity != null) {
			return entity;
		}
		return doFindById(id);
	}

	private E doFindById(I id) {
		if (!mock) {
			return findByIdFromStore(id);
		} else {
			return mockStore.get(id);
		}
	}

	protected abstract E findByIdFromStore(I id);

	public void save(E entity) {

		ProcessContext processContext = ThreadBoundProcessContextArray
				.getProcessContext();
		if (!processContext.isStarted()) {
			throw new RuntimeException(
					"can not use repository without a process");
		}

		I id = getId(entity);
		processContext.putEntityInProcess(this.id, id, entity);

	}

	public E saveIfAbsent(E entity) {
		ProcessContext processContext = ThreadBoundProcessContextArray
				.getProcessContext();
		if (!processContext.isStarted()) {
			throw new RuntimeException(
					"can not use repository without a process");
		}

		I id = getId(entity);

		ProcessEntity<E> processEntity = processContext
				.putIfAbsentEntityInProcess(this.id, id, entity);
		if (processEntity != null) {
			ProcessEntityState entityState = processEntity.getState();
			if (!(entityState instanceof TransientProcessEntityState)) {
				return processEntity.getEntity();
			}
		}

		E entityFromStore = doSaveIfAbsent(id, entity);
		if (entityFromStore != null) {
			processContext.takeEntityFromRepoAndPutInProcess(this.id, id,
					entityFromStore);
		} else {
			processContext.takeEntityFromRepoAndPutInProcess(this.id, id,
					entity);
		}
		return entityFromStore;
	}

	private E doSaveIfAbsent(I id, E entity) {
		if (!mock) {
			return saveIfAbsentToStore(id, entity);
		} else {
			return mockStore.putIfAbsent(id, entity);
		}
	}

	protected abstract E saveIfAbsentToStore(I id, E entity);

	public E remove(I id) {
		ProcessContext processContext = ThreadBoundProcessContextArray
				.getProcessContext();
		if (!processContext.isStarted()) {
			throw new RuntimeException(
					"can not use repository without a process");
		}
		ProcessEntity<E> processEntity = processContext.removeEntityInProcess(
				this.id, id);
		if (processEntity != null) {
			return processEntity.getEntity();
		}

		E entityFromStore = doFindByIdForUpdate(id);
		if (entityFromStore != null) {
			processContext.takeEntityFromRepoAndPutInProcessAsRemoved(this.id,
					id, entityFromStore);
		}
		return entityFromStore;

	}

	public void deleteEntities(Set<I> ids) {

		if (!mock) {
			removeAllToStore(ids);
		} else {
			for (I id : ids) {
				mockStore.remove(id);
			}
		}

	}

	protected abstract void removeAllToStore(Set<I> ids);

	public void updateEntities(Map<I, E> entitiesToReturn) {

		if (!mock) {
			updateAllToStore(entitiesToReturn);
		} else {
		}

	}

	protected abstract void updateAllToStore(Map<I, E> entities);

	public void createEntities(Map<I, E> entitiesToCreate) {

		if (!mock) {
			saveAllToStore(entitiesToCreate);
		} else {
			mockStore.putAll(entitiesToCreate);
		}

	}

	protected abstract void saveAllToStore(Map<I, E> entities);

	public void returnEntities(Set<I> ids) {
		if (!mock) {
			unlockAllToStore(ids);
		} else {
		}
	}

	protected abstract void unlockAllToStore(Set<I> ids);
}
