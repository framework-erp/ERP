package arp.process;

import arp.AppContext;
import arp.process.states.CreatedInProcState;
import arp.process.states.TakenFromRepoState;
import arp.process.states.ToRemoveInRepoState;
import arp.repository.InnerRepository;
import arp.repository.InnerSingletonRepository;
import arp.repository.RepositoryProcessEntities;
import arp.util.Unsafe;

import java.util.*;
import java.util.Map.Entry;

public class ProcessContext {

    private boolean started;

    private String processName;

    private Map<String, RepositoryProcessEntities<?, ?>> processEntities = new HashMap<>();

    private List<String> singletonTypes = new ArrayList<>();

    private List<Object> argumentList = new ArrayList<>();

    private Object result;

    private List<Object> createdEntityList = new ArrayList<>();
    private List<Object> deletedEntityList = new ArrayList<>();
    private List<Object[]> updatedEntityList = new ArrayList<>();

    public void startProcess(String processName) {
        if (started) {
            throw new RuntimeException("can not start a process in another started process");
        }
        clear();
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
                    createdEntityList.add(processEntity.getEntity());
                } else if (processEntity.getState() instanceof TakenFromRepoState) {
                    if (processEntity.changed()) {
                        entitiesToUpdate.put(id, processEntity);
                        updatedEntityList.add(new Object[]{processEntity.getInitialEntitySnapshot(), processEntity.getEntity()});
                    }
                } else if (processEntity.getState() instanceof ToRemoveInRepoState) {
                    idsToRemoveEntity.add(id);
                    deletedEntityList.add(processEntity.getEntity());
                }
            }
            InnerRepository repository = AppContext.getRepository(repoPes.getEntityType());
            repository.flushProcessEntities(entitiesToInsert, entitiesToUpdate, idsToRemoveEntity);
        }
    }

    public <I, E> ProcessEntity<E> takeEntityInProcess(String entityType, I entityId) {
        RepositoryProcessEntities<I, E> entities = (RepositoryProcessEntities<I, E>) processEntities.get(entityType);
        if (entities == null) {
            return null;
        }
        return entities.takeProcessEntity(entityId);
    }

    public <I, E> E copyEntityInProcess(String entityType, I entityId) {
        RepositoryProcessEntities<I, E> entities = (RepositoryProcessEntities<I, E>) processEntities.get(entityType);
        if (entities == null) {
            return null;
        }
        return entities.copyEntity(entityId);
    }

    public <I, E> E getEntityInProcess(String entityType, I entityId) {
        RepositoryProcessEntities<I, E> entities = (RepositoryProcessEntities<I, E>) processEntities.get(entityType);
        if (entities == null) {
            return null;
        }
        return entities.getEntity(entityId);
    }

    public <I, E> void removeEntityInProcess(String entityType, I entityId) {
        RepositoryProcessEntities<I, E> entities = (RepositoryProcessEntities<I, E>) processEntities.get(entityType);
        if (entities == null) {
            return;
        }
        entities.removeEntity(entityId);
    }

    public <I, E> void addEntityTakenFromRepo(String entityType, I entityId, E entity) {
        RepositoryProcessEntities<I, E> entities = (RepositoryProcessEntities<I, E>) processEntities.get(entityType);
        if (entities == null) {
            entities = new RepositoryProcessEntities<>(entityType);
            processEntities.put(entityType, entities);
        }
        entities.addEntityTaken(entityId, entity);
    }

    public <I, E> void addNewEntity(String entityType, I entityId, E entity) {
        RepositoryProcessEntities<I, E> entities = (RepositoryProcessEntities<I, E>) processEntities.get(entityType);
        if (entities == null) {
            entities = new RepositoryProcessEntities<>(entityType);
            processEntities.put(entityType, entities);
        }
        entities.addNewEntity(entityId, entity);
    }

    public void processFaild() {
        try {
            releaseProcessEntities();
        } catch (Exception e) {
        }
        started = false;
    }

    public void clear() {
        processEntities.clear();
        argumentList.clear();
        createdEntityList.clear();
        deletedEntityList.clear();
        updatedEntityList.clear();
        result = null;
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

            InnerRepository repository = AppContext.getRepository(repoPes.getEntityType());
            repository.releaseProcessEntity(ids);

        }
        for (String type : singletonTypes) {
            InnerSingletonRepository repository = AppContext.getSingletonRepository(type);
            repository.releaseProcessEntity();
        }
    }

    public void addEntityTakenFromSingletonRepo(String entityType) {
        singletonTypes.add(entityType);
    }

    public void recordProcessResult(Object result) {
        this.result = result;
    }

    public void recordProcessArgument(Object argument) {
        argumentList.add(argument);
    }

    public <ID, E> boolean entityAvailableInProcess(String entityType, ID entityId) {
        RepositoryProcessEntities<ID, E> entities = (RepositoryProcessEntities<ID, E>) processEntities.get(entityType);
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
        process.setUpdatedEntityList(new ArrayList<>(updatedEntityList));
        process.setDeletedEntityList(new ArrayList<>(deletedEntityList));
        process.setResult(result);
        return process;
    }

}
