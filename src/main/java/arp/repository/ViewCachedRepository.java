package arp.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import arp.process.ProcessContext;
import arp.process.ThreadBoundProcessContextArray;
import arp.repository.copy.EntityCopier;

/**
 * 用于只读查询类场景加速。
 * 
 * @author neo
 *
 */
public class ViewCachedRepository<E, I> extends Repository<E, I> {

	protected Map<I, Object> cache;
	protected Repository<E, I> underlyingRepository;
	protected Class<E> entityType;

	public ViewCachedRepository() {
		initAsMock();
	}

	public ViewCachedRepository(Repository<E, I> underlyingRepository,
			Class<E> entityType) {
		if (underlyingRepository.mock) {
			initAsMock();
			return;
		}
		this.underlyingRepository = underlyingRepository;
		this.cache = new ConcurrentHashMap<>();
		this.entityType = entityType;
		ViewCachedRepositorySynchronizer.registerRepository(this, entityType);
	}

	@Override
	protected E findByIdForUpdateFromStore(I id) {
		E entity = underlyingRepository.findByIdForUpdateFromStore(id);
		updateCacheForEntity(id, entity);
		return entity;
	}

	private void updateCacheForEntity(I id, E entity) {
		cache.put(id, entity == null ? new NullEntity() : entity);
	}

	@Override
	protected E findByIdFromStore(I id) {
		Object entity = cache.get(id);
		if (entity != null) {
			return entity instanceof NullEntity ? null : (E) EntityCopier
					.copy(entity);
		}
		E entityFromStore = underlyingRepository.findByIdFromStore(id);
		updateCacheForEntity(id, entityFromStore);
		entity = cache.get(id);
		return entity instanceof NullEntity ? null : (E) EntityCopier
				.copy(entity);
	}

	@Override
	protected E saveIfAbsentToStore(I id, E entity) {
		E existsEntity = underlyingRepository.saveIfAbsentToStore(id, entity);
		updateCacheForEntity(id, existsEntity != null ? existsEntity : entity);
		recordIdForUpdate(id);
		return existsEntity;
	}

	private void recordIdForUpdate(I id) {
		ProcessContext processContext = ThreadBoundProcessContextArray
				.getProcessContext();
		Map map = (Map) processContext
				.getContextParameter("idsForEntityUpdatedWithViewCachedRepository");
		if (map == null) {
			map = new HashMap();
			processContext.addContextParameter(
					"idsForEntityUpdatedWithViewCachedRepository", map);
		}
		List idsForThisRepo = (List) map.get(entityType.getName());
		if (idsForThisRepo == null) {
			idsForThisRepo = new ArrayList();
			map.put(entityType.getName(), idsForThisRepo);
		}
		idsForThisRepo.add(id);
	}

	@Override
	protected void removeAllToStore(Set<I> ids) {
		underlyingRepository.removeAllToStore(ids);
		for (I id : ids) {
			updateCacheForEntity(id, null);
			recordIdForUpdate(id);
		}
	}

	@Override
	protected void updateAllToStore(Map<I, E> entities) {
		underlyingRepository.updateAllToStore(entities);
		for (Entry<I, E> entry : entities.entrySet()) {
			updateCacheForEntity(entry.getKey(), entry.getValue());
			recordIdForUpdate(entry.getKey());
		}
	}

	@Override
	protected void saveAllToStore(Map<I, E> entities) {
		underlyingRepository.saveAllToStore(entities);
		for (Entry<I, E> entry : entities.entrySet()) {
			updateCacheForEntity(entry.getKey(), entry.getValue());
			recordIdForUpdate(entry.getKey());
		}
	}

	@Override
	protected void unlockAllToStore(Set<I> ids) {
		underlyingRepository.unlockAllToStore(ids);
	}

	@Override
	protected I getId(E entity) {
		return underlyingRepository.getId(entity);
	}

	public void updateCacheFromUnderlyingForEntity(I id) {
		E entity = underlyingRepository.findByIdFromStore(id);
		cache.put(id, entity == null ? new NullEntity() : entity);
	}

}
