package arp.repository;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import arp.process.CreatedProcessEntityState;
import arp.process.ProcessContext;
import arp.process.ProcessEntity;
import arp.process.ProcessEntityState;
import arp.process.TakenProcessEntityState;
import arp.process.ThreadBoundProcessContextArray;
import arp.process.TransientProcessEntityState;
import arp.util.Unsafe;

/**
 * 仓库是存放聚合的地方，聚合只会通过它的id来获取。
 *
 * @param <E> 实体类型
 * @param <ID> ID类型
 */
public abstract class Repository<E, ID> {

	private static AtomicInteger ids = new AtomicInteger();

	private static Repository[] repositories = new Repository[1024];

	private int id;

	private Function<Object[], Object> setIdFunction = null;

	private Map<ID, E> mockStore;

	protected boolean mock = false;

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

	protected abstract ID getId(E entity);

	public E take(ID id) {
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

	private E doFindByIdForUpdate(ID id) {
		if (!mock) {
			return findByIdForUpdateFromStore(id);
		} else {
			return mockStore.get(id);
		}
	}

	protected abstract E findByIdForUpdateFromStore(ID id);

	public E find(ID id) {
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

	private E doFindById(ID id) {
		if (!mock) {
			return findByIdFromStore(id);
		} else {
			return mockStore.get(id);
		}
	}

	protected abstract E findByIdFromStore(ID id);

	public void put(E entity) {

		ProcessContext processContext = ThreadBoundProcessContextArray
				.getProcessContext();
		if (!processContext.isStarted()) {
			throw new RuntimeException(
					"can not use repository without a process");
		}

		ID id = getId(entity);
		processContext.putEntityInProcess(this.id, id, entity);

	}

	public E putIfAbsent(E entity) {
		ProcessContext processContext = ThreadBoundProcessContextArray
				.getProcessContext();
		if (!processContext.isStarted()) {
			throw new RuntimeException(
					"can not use repository without a process");
		}

		ID id = getId(entity);

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
			processContext.addCreatedAggr(entity);
			processContext.takeEntityFromRepoAndPutInProcess(this.id, id,
					entity);
		}
		return entityFromStore;
	}

	private E doSaveIfAbsent(ID id, E entity) {
		if (!mock) {
			return saveIfAbsentToStore(id, entity);
		} else {
			return mockStore.putIfAbsent(id, entity);
		}
	}

	protected abstract E saveIfAbsentToStore(ID id, E entity);

	public E remove(ID id) {
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

	public void deleteEntities(Set<ID> ids) {

		if (!mock) {
			removeAllToStore(ids);
		} else {
			for (ID id : ids) {
				mockStore.remove(id);
			}
		}

	}

	protected abstract void removeAllToStore(Set<ID> ids);

	public void updateEntities(Map<ID, E> entitiesToReturn) {

		if (!mock) {
			updateAllToStore(entitiesToReturn);
		} else {
		}

	}

	protected abstract void updateAllToStore(Map<ID, E> entities);

	public void createEntities(Map<ID, E> entitiesToCreate) {

		if (!mock) {
			saveAllToStore(entitiesToCreate);
		} else {
			mockStore.putAll(entitiesToCreate);
		}

	}

	protected abstract void saveAllToStore(Map<ID, E> entities);

	public void returnEntities(Set<ID> ids) {
		if (!mock) {
			unlockAllToStore(ids);
		} else {
		}
	}

	protected abstract void unlockAllToStore(Set<ID> ids);

	public E findByIdForUpdateOrCreateAndLock(ID id, E newEntity) {
		E entity = take(id);
		if (entity == null) {
			if (setIdFunction == null) {
				try {
					createSetIdFunction(newEntity);
				} catch (Exception e) {
				}
			}
			setIdFunction.apply(new Object[] { newEntity, id });
			E existsEntity = putIfAbsent(newEntity);
			if (existsEntity != null) {
				return existsEntity;
			}
			return newEntity;
		}
		return entity;
	}

	private void createSetIdFunction(E newEntity) throws Exception {
		Field idField = newEntity.getClass().getDeclaredField("id");
		long idFieldOffset = Unsafe.getFieldOffset(idField);
		Class<?> idFieldType = idField.getType();
		if (idFieldType.equals(byte.class)) {
			setIdFunction = new Function<Object[], Object>() {

				@Override
				public Object apply(Object[] t) {
					Unsafe.setByteFieldOfObject(t[0], idFieldOffset,
							((Byte) t[1]).byteValue());
					return null;
				}

			};
		} else if (idFieldType.equals(short.class)) {
			setIdFunction = new Function<Object[], Object>() {

				@Override
				public Object apply(Object[] t) {
					Unsafe.setShortFieldOfObject(t[0], idFieldOffset,
							((Short) t[1]).shortValue());
					return null;
				}

			};
		} else if (idFieldType.equals(char.class)) {
			setIdFunction = new Function<Object[], Object>() {

				@Override
				public Object apply(Object[] t) {
					Unsafe.setCharFieldOfObject(t[0], idFieldOffset,
							((Character) t[1]).charValue());
					return null;
				}

			};
		} else if (idFieldType.equals(int.class)) {
			setIdFunction = new Function<Object[], Object>() {

				@Override
				public Object apply(Object[] t) {
					Unsafe.setIntFieldOfObject(t[0], idFieldOffset,
							((Integer) t[1]).intValue());
					return null;
				}

			};
		} else if (idFieldType.equals(float.class)) {
			setIdFunction = new Function<Object[], Object>() {

				@Override
				public Object apply(Object[] t) {
					Unsafe.setFloatFieldOfObject(t[0], idFieldOffset,
							((Float) t[1]).floatValue());
					return null;
				}

			};
		} else if (idFieldType.equals(long.class)) {
			setIdFunction = new Function<Object[], Object>() {

				@Override
				public Object apply(Object[] t) {
					Unsafe.setLongFieldOfObject(t[0], idFieldOffset,
							((Long) t[1]).longValue());
					return null;
				}

			};
		} else if (idFieldType.equals(double.class)) {
			setIdFunction = new Function<Object[], Object>() {

				@Override
				public Object apply(Object[] t) {
					Unsafe.setDoubleFieldOfObject(t[0], idFieldOffset,
							((Double) t[1]).doubleValue());
					return null;
				}

			};
		} else if (idFieldType.equals(boolean.class)) {
			setIdFunction = new Function<Object[], Object>() {

				@Override
				public Object apply(Object[] t) {
					Unsafe.setBooleanFieldOfObject(t[0], idFieldOffset,
							((Boolean) t[1]).booleanValue());
					return null;
				}

			};
		} else {
			setIdFunction = new Function<Object[], Object>() {

				@Override
				public Object apply(Object[] t) {
					Unsafe.setObjectFieldOfObject(t[0], idFieldOffset, t[1]);
					return null;
				}

			};
		}

	}
}
