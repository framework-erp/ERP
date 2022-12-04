package arp.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import arp.AppContext;
import arp.enhance.ProcessInfo;
import arp.process.states.CreatedInProcState;
import arp.process.states.TakenFromRepoState;
import arp.process.states.ToRemoveInRepoState;
import arp.repository.InnerRepository;
import arp.repository.Repository;
import arp.repository.RepositoryProcessEntities;
import arp.util.Unsafe;

public class ProcessContext {

    private static ProcessInfo[] processInfos;

    public static void setProcessInfos(List<ProcessInfo> processInfoList) {
        processInfos = new ProcessInfo[processInfoList.size()];
        for (ProcessInfo processInfo : processInfoList) {
            processInfos[processInfo.getId()] = processInfo;
        }
    }

    public static ProcessInfo getProcessInfo(int processInfoId) {
        return processInfos[processInfoId];
    }

    private boolean started;

    private Map<String, RepositoryProcessEntities<?, ?>> processEntities = new HashMap<>();

    private List<AtomicInteger> singleEntityAcquiredLocks = new ArrayList<>();

    private List<Object> arguments = new ArrayList<>();

    private Object result;

    private List<Object> createdAggrs = new ArrayList<>();
    private List<Object> deletedAggrs = new ArrayList<>();
    private List<Object[]> updatedAggrs = new ArrayList<>();

    private boolean dontPublishWhenResultIsNull;

    private boolean publish;

    private String processName;

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
                clear();
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
                    createdAggrs.add(processEntity.getEntity());
                } else if (processEntity.getState() instanceof TakenFromRepoState) {
                    if (processEntity.changed()) {
                        entitiesToUpdate.put(id, processEntity);
                        updatedAggrs.add(new Object[]{processEntity.getInitialEntitySnapshot(), processEntity.getEntity()});
                    }
                } else if (processEntity.getState() instanceof ToRemoveInRepoState) {
                    idsToRemoveEntity.add(id);
                    deletedAggrs.add(processEntity.getEntity());
                }
            }
            InnerRepository repository = AppContext.getRepository(repoPes.getAggType());
            repository.flushProcessEntities(entitiesToInsert, entitiesToUpdate, idsToRemoveEntity);
        }
    }

    public <I, E> ProcessEntity<E> takeEntityInProcess(String aggType, I entityId) {
        RepositoryProcessEntities<I, E> entities = (RepositoryProcessEntities<I, E>) processEntities.get(aggType);
        if (entities == null) {
            return null;
        }
        return entities.takeEntity(entityId);
    }

    public <I, E> E copyEntityInProcess(String aggType, I entityId) {
        RepositoryProcessEntities<I, E> entities = (RepositoryProcessEntities<I, E>) processEntities.get(aggType);
        if (entities == null) {
            return null;
        }
        return entities.copyEntity(entityId);
    }

    public <I, E> E getEntityInProcess(String aggType, I entityId) {
        RepositoryProcessEntities<I, E> entities = (RepositoryProcessEntities<I, E>) processEntities.get(aggType);
        if (entities == null) {
            return null;
        }
        return entities.getEntity(entityId);
    }

    public <I, E> void removeEntityInProcess(String aggType, I entityId) {
        RepositoryProcessEntities<I, E> entities = (RepositoryProcessEntities<I, E>) processEntities.get(aggType);
        if (entities == null) {
            return;
        }
        entities.removeEntity(entityId);
    }

    public <I, E> void addEntityTakenFromRepo(String aggType, I entityId, E entity) {
        RepositoryProcessEntities<I, E> entities = (RepositoryProcessEntities<I, E>) processEntities.get(aggType);
        if (entities == null) {
            entities = new RepositoryProcessEntities<>(aggType);
            processEntities.put(aggType, entities);
        }
        entities.addEntityTaken(entityId, entity);
    }

    public <I, E> void addNewEntity(String aggType, I entityId, E entity) {
        RepositoryProcessEntities<I, E> entities = (RepositoryProcessEntities<I, E>) processEntities.get(aggType);
        if (entities == null) {
            entities = new RepositoryProcessEntities<>(aggType);
            processEntities.put(aggType, entities);
        }
        entities.addNewEntity(entityId, entity);
    }

    public void processFaild() {
        try {
            releaseProcessEntities();
        } catch (Exception e) {
        }
        clear();
        started = false;
    }

    public void clear() {
        processEntities.clear();
        arguments.clear();
        createdAggrs.clear();
        deletedAggrs.clear();
        updatedAggrs.clear();
        result = null;
        dontPublishWhenResultIsNull = false;
        publish = false;
    }

    private void releaseProcessEntities() throws Exception {
        for (RepositoryProcessEntities repoPes : processEntities.values()) {

            Map processEntities = repoPes.getEntities();
            Set<Object> ids = new HashSet<>();

            for (Object obj : processEntities.entrySet()) {
                Entry entry = (Entry) obj;
                Object id = entry.getKey();
                ProcessEntity processEntity = (ProcessEntity) entry.getValue();
                if (processEntity.isAvailable()) {
                    ids.add(id);
                }
            }

            InnerRepository repository =AppContext.getRepository(repoPes.getAggType());
            repository.releaseProcessEntity(ids);

        }
        for (AtomicInteger lock : singleEntityAcquiredLocks) {
            lock.set(0);
        }
    }

    public void addSingleEntityAcquiredLock(AtomicInteger lock) {
        singleEntityAcquiredLocks.add(lock);
    }

    public void recordProcessResult(Object result) {
        this.result = result;
    }

    public void setDontPublishWhenResultIsNull(boolean dontPublishWhenResultIsNull) {
        this.dontPublishWhenResultIsNull = dontPublishWhenResultIsNull;
    }

    public void recordProcessDesc(String clsName, String mthName, String processName) {
        if (!processName.trim().isEmpty()) {
            processDesc = processName;
        } else {
            processDesc = clsName + "." + mthName;
        }
    }

    public Object getResult() {
        return result;
    }

    public boolean isDontPublishWhenResultIsNull() {
        return dontPublishWhenResultIsNull;
    }

    public String getProcessDesc() {
        return processDesc;
    }

    public boolean isPublish() {
        return publish;
    }

    public void setPublish(boolean publish) {
        this.publish = publish;
    }

    public void recordProcessArgument(Object argument) {
        arguments.add(argument);
    }

    public List<Object> getArguments() {
        return arguments;
    }

    public List<Object[]> getUpdatedAggrs() {
        return updatedAggrs;
    }

    public List<Object> getCreatedAggrs() {
        return createdAggrs;
    }

    public List<Object> getDeletedAggrs() {
        return deletedAggrs;
    }

    public int getProcessInfoId() {
        return processInfoId;
    }

    public ProcessInfo getProcessInfo() {
        return processInfos[processInfoId];
    }

    public <ID, E> boolean entityAvailableInProcess(String aggType, ID entityId) {
        RepositoryProcessEntities<ID, E> entities = (RepositoryProcessEntities<ID, E>) processEntities.get(aggType);
        if (entities == null) {
            return false;
        }
        ProcessEntity<E> processEntity = entities.takeEntity(entityId);
        if (processEntity == null) {
            return false;
        }
        return processEntity.isAvailable();
    }

}
