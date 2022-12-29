package arp.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import arp.AppContext;
import arp.process.states.CreatedInProcState;
import arp.process.states.TakenFromRepoState;
import arp.process.states.ToRemoveInRepoState;
import arp.repository.InnerRepository;
import arp.repository.InnerSingletonRepository;
import arp.repository.RepositoryProcessEntities;
import arp.util.Unsafe;

public class ProcessContext {

    private boolean started;

    private Map<String, RepositoryProcessEntities<?, ?>> processEntities = new HashMap<>();

    private List<String> singletonTypes = new ArrayList<>();

    private List<Object> arguments = new ArrayList<>();

    private Object result;

    private List<Object> createdAggrs = new ArrayList<>();
    private List<Object> deletedAggrs = new ArrayList<>();
    private List<Object[]> updatedAggrs = new ArrayList<>();

    private String processName;

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
        return entities.takeProcessEntity(entityId);
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
        started = false;
    }

    public void clear() {
        processEntities.clear();
        arguments.clear();
        createdAggrs.clear();
        deletedAggrs.clear();
        updatedAggrs.clear();
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

            InnerRepository repository = AppContext.getRepository(repoPes.getAggType());
            repository.releaseProcessEntity(ids);

        }
        for (String type : singletonTypes) {
            InnerSingletonRepository repository = AppContext.getSingletonRepository(type);
            repository.releaseProcessEntity();
        }
    }

    public void addEntityTakenFromSingletonRepo(String aggType) {
        singletonTypes.add(aggType);
    }

    public void recordProcessResult(Object result) {
        this.result = result;
    }

    public void recordProcessArgument(Object argument) {
        arguments.add(argument);
    }

    public <ID, E> boolean entityAvailableInProcess(String aggType, ID entityId) {
        RepositoryProcessEntities<ID, E> entities = (RepositoryProcessEntities<ID, E>) processEntities.get(aggType);
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
        process.setArguments(new ArrayList<>(arguments));
        process.setCreatedAggrs(new ArrayList<>(createdAggrs));
        process.setUpdatedAggrs(new ArrayList<>(updatedAggrs));
        process.setDeletedAggrs(new ArrayList<>(deletedAggrs));
        process.setResult(result);
        return process;
    }

}
