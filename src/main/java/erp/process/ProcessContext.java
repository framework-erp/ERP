package erp.process;

import erp.AppContext;
import erp.process.definition.*;
import erp.process.definition.Process;
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

    private Object result;

    private List<TypedEntity> createdEntityList = new ArrayList<>();
    private List<TypedEntity> deletedEntityList = new ArrayList<>();
    private List<TypedEntityUpdate> entityUpdateList = new ArrayList<>();

    //用于记录从仓库中删除的实体，后面释放锁就不用释放了，删除实体同时也会删除锁
    private Map<String, Set<Object>> repositoryEntityIdsToRemove = new HashMap<>();

    public void startProcess(String processName) {
        if (started) {
            throw new RuntimeException("can not start a process in another started process");
        }
        clear();
        this.processName = processName;
        started = true;
        Unsafe.loadFence();
    }

    public void finishProcess() throws Exception {
        Unsafe.storeFence();
        flushProcessEntities();
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

    public void releaseProcessEntities() throws Exception {
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
        this.result = result;
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

    public void setStarted(boolean started) {
        this.started = started;
    }

    public void clear() {
        processEntities.clear();
        argumentList.clear();
        createdEntityList.clear();
        deletedEntityList.clear();
        entityUpdateList.clear();
        result = null;
        repositoryEntityIdsToRemove.clear();
    }

    public Process buildProcess() {
        Process process = new Process();
        process.setName(processName);
        List<TypedArgument> argumentList = new ArrayList<>();
        for (Object arg : this.argumentList) {
            argumentList.add(new TypedArgument(arg.getClass().getName(), arg));
        }
        process.setArgumentList(argumentList);
        process.setCreatedEntityList(new ArrayList<>(createdEntityList));
        process.setEntityUpdateList(new ArrayList<>(entityUpdateList));
        process.setDeletedEntityList(new ArrayList<>(deletedEntityList));
        TypedObject result = this.result == null ? null : new TypedObject(this.result.getClass().getName(), this.result);
        process.setResult(result);
        return process;
    }

}
