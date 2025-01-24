package erp.process;

import erp.AppContext;
import erp.process.definition.Process;
import erp.process.definition.TypedEntity;
import erp.process.definition.TypedEntityUpdate;
import erp.process.definition.TypedResult;
import erp.process.states.CreatedInProcState;
import erp.process.states.TakenFromRepoState;
import erp.process.states.ToRemoveInRepoState;
import erp.repository.InnerRepository;
import erp.repository.copy.EntityCopier;
import erp.util.Unsafe;

import java.util.*;
import java.util.Map.Entry;

public class ProcessContext {

    private boolean started;

    private String processName;

    private Map<String, RepositoryProcessEntities<?, ?>> processEntities = new HashMap<>();

    private List<Object> argumentList = new ArrayList<>();

    private TypedResult result;

    private List<TypedEntity> createdEntityList = new ArrayList<>();
    private List<TypedEntity> deletedEntityList = new ArrayList<>();
    private List<TypedEntityUpdate> entityUpdateList = new ArrayList<>();

    //用于记录从仓库中删除的实体，后面释放锁就不用释放了，删除实体同时也会删除锁
    private Map<String, Set<Object>> repositoryEntityIdsToRemove = new HashMap<>();

    public void startProcess(String processName) {
        if (started) {
            throw new RuntimeException("can not start a process in another started process");
        }
        this.processName = processName;
        started = true;
        Unsafe.loadFence();
    }

    public void finishProcess() {
        Unsafe.storeFence();
        try {
            flushProcessEntities();
        } catch (Exception e) {
            throw new RuntimeException("flush process entities faild", e);
        } finally {
            try {
                releaseProcessEntities();
            } catch (Exception e) {
                throw new RuntimeException("release process entities faild", e);
            } finally {
                started = false;
            }
        }
    }

    private void flushProcessEntities() throws Exception {
        for (RepositoryProcessEntities repoPes : processEntities.values()) {
            Map processEntities = repoPes.getEntities();
            Map<Object, Object> entitiesToInsert = new HashMap<>();
            Map<Object, ProcessEntity> entitiesToUpdate = new HashMap<>();
            Set<Object> idsToRemoveEntity = new HashSet<>();

            for (Object obj : processEntities.entrySet()) {
                Entry entry = (Entry) obj;
                Object id = entry.getKey();
                ProcessEntity processEntity = (ProcessEntity) entry.getValue();
                if (processEntity.getState() instanceof CreatedInProcState) {
                    entitiesToInsert.put(id, processEntity.getEntity());
                    createdEntityList.add(new TypedEntity(processEntity.getEntity(), repoPes.getRepositoryName()));
                } else if (processEntity.getState() instanceof TakenFromRepoState) {
                    if (processEntity.changed()) {
                        entitiesToUpdate.put(id, processEntity);
                        entityUpdateList.add(new TypedEntityUpdate(processEntity.getInitialEntitySnapshot(), processEntity.getEntity(), repoPes.getRepositoryName()));
                    }
                } else if (processEntity.getState() instanceof ToRemoveInRepoState) {
                    idsToRemoveEntity.add(id);
                    deletedEntityList.add(new TypedEntity(processEntity.getEntity(), repoPes.getRepositoryName()));
                }
            }
            repositoryEntityIdsToRemove.put(repoPes.getRepositoryName(), idsToRemoveEntity);
            InnerRepository repository = AppContext.getRepository(repoPes.getRepositoryName());
            repository.flushProcessEntities(entitiesToInsert, entitiesToUpdate, idsToRemoveEntity);
        }

    }

    public <I, E> ProcessEntity<E> getEntityInProcess(String repositoryName, I entityId) {
        RepositoryProcessEntities<I, E> entities = (RepositoryProcessEntities<I, E>) processEntities.get(repositoryName);
        if (entities == null) {
            return null;
        }
        return entities.getProcessEntity(entityId);
    }

    public <I, E> ProcessEntity<E> takeEntityInProcess(String repositoryName, I entityId) {
        RepositoryProcessEntities<I, E> entities = (RepositoryProcessEntities<I, E>) processEntities.get(repositoryName);
        if (entities == null) {
            return null;
        }
        return entities.takeProcessEntity(entityId);
    }

    public <I, E> E copyEntityInProcess(String repositoryName, I entityId) {
        RepositoryProcessEntities<I, E> entities = (RepositoryProcessEntities<I, E>) processEntities.get(repositoryName);
        if (entities == null) {
            return null;
        }
        return entities.copyEntity(entityId);
    }

    public <I, E> void removeEntityInProcess(String repositoryName, I entityId) {
        RepositoryProcessEntities<I, E> entities = (RepositoryProcessEntities<I, E>) processEntities.get(repositoryName);
        if (entities == null) {
            return;
        }
        entities.removeEntity(entityId);
    }

    public <I, E> void addEntityTakenFromRepo(String repositoryName, I entityId, E entity) {
        RepositoryProcessEntities<I, E> entities = (RepositoryProcessEntities<I, E>) processEntities.get(repositoryName);
        if (entities == null) {
            entities = new RepositoryProcessEntities<>(repositoryName);
            processEntities.put(repositoryName, entities);
        }
        entities.addEntityTaken(entityId, entity);
    }

    public <I, E> void addNewEntity(String repositoryName, I entityId, E entity) {
        RepositoryProcessEntities<I, E> entities = (RepositoryProcessEntities<I, E>) processEntities.get(repositoryName);
        if (entities == null) {
            entities = new RepositoryProcessEntities<>(repositoryName);
            processEntities.put(repositoryName, entities);
        }
        entities.addNewEntity(entityId, entity);
    }

    public <I, E> void addEntityCreatedAndTakenFromRepo(String repositoryName, I entityId, E entity) {
        createdEntityList.add(new TypedEntity(EntityCopier.copy(entity), repositoryName));
        addEntityTakenFromRepo(repositoryName, entityId, entity);
    }

    public void processFaild() {
        try {
            releaseProcessEntities();
        } catch (Exception e) {
        }
        started = false;
    }

    private void releaseProcessEntities() throws Exception {
        for (RepositoryProcessEntities repoPes : processEntities.values()) {

            Map processEntities = repoPes.getEntities();
            Set<Object> ids = new HashSet<>();
            Set<Object> idsToRemoveEntity = repositoryEntityIdsToRemove.get(repoPes.getRepositoryName());

            for (Object obj : processEntities.entrySet()) {
                Entry entry = (Entry) obj;
                Object id = entry.getKey();
                ProcessEntity processEntity = (ProcessEntity) entry.getValue();
                if (processEntity.isAddByTake()
                        && (idsToRemoveEntity == null || !idsToRemoveEntity.contains(id))) {
                    ids.add(id);
                }
            }

            InnerRepository repository = AppContext.getRepository(repoPes.getRepositoryName());
            repository.releaseProcessEntity(ids);

        }
    }

    public void recordProcessResult(Object result) {
        if (result == null) {
            return;
        }
        this.result = new TypedResult(result.getClass().getName(), result);
    }

    public void recordProcessArgument(Object argument) {
        argumentList.add(argument);
    }

    public <ID, E> boolean entityAvailableInProcess(String repositoryName, ID entityId) {
        RepositoryProcessEntities<ID, E> entities = (RepositoryProcessEntities<ID, E>) processEntities.get(repositoryName);
        if (entities == null) {
            return false;
        }
        ProcessEntity<E> processEntity = entities.getProcessEntity(entityId);
        if (processEntity == null) {
            return false;
        }
        return processEntity.isAvailable();
    }

    public String getProcessName() {
        return processName;
    }

    public boolean isStarted() {
        return started;
    }

    public Process buildProcess() {
        Process process = new Process();
        process.setName(processName);
        process.setArgumentList(new ArrayList<>(argumentList));
        process.setCreatedEntityList(new ArrayList<>(createdEntityList));
        process.setEntityUpdateList(new ArrayList<>(entityUpdateList));
        process.setDeletedEntityList(new ArrayList<>(deletedEntityList));
        process.setResult(result);
        return process;
    }

}
