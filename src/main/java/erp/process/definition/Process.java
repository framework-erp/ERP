package erp.process.definition;

import java.util.List;

/**
 * 完整描述过程的标准数据结构
 */
public class Process {
    private String name;
    private List<Object> argumentList;
    private Object result;
    private List<TypedEntity> createdEntityList;
    private List<TypedEntity> deletedEntityList;
    private List<TypedEntityUpdate> entityUpdateList;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Object> getArgumentList() {
        return argumentList;
    }

    public void setArgumentList(List<Object> argumentList) {
        this.argumentList = argumentList;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public List<TypedEntity> getCreatedEntityList() {
        return createdEntityList;
    }

    public void setCreatedEntityList(List<TypedEntity> createdEntityList) {
        this.createdEntityList = createdEntityList;
    }

    public List<TypedEntity> getDeletedEntityList() {
        return deletedEntityList;
    }

    public void setDeletedEntityList(List<TypedEntity> deletedEntityList) {
        this.deletedEntityList = deletedEntityList;
    }

    public List<TypedEntityUpdate> getEntityUpdateList() {
        return entityUpdateList;
    }

    public void setEntityUpdateList(List<TypedEntityUpdate> entityUpdateList) {
        this.entityUpdateList = entityUpdateList;
    }
    
}
