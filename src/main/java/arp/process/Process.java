package arp.process;

import java.util.List;

/**
 * 完整描述过程的标准数据结构
 */
public class Process {
    private String name;
    private List<Object> argumentList;
    private Object result;
    private List<Object> createdEntityList;
    private List<Object> deletedEntityList;
    private List<Object[]> updatedEntityList;

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

    public List<Object> getCreatedEntityList() {
        return createdEntityList;
    }

    public void setCreatedEntityList(List<Object> createdEntityList) {
        this.createdEntityList = createdEntityList;
    }

    public List<Object> getDeletedEntityList() {
        return deletedEntityList;
    }

    public void setDeletedEntityList(List<Object> deletedEntityList) {
        this.deletedEntityList = deletedEntityList;
    }

    public List<Object[]> getUpdatedEntityList() {
        return updatedEntityList;
    }

    public void setUpdatedEntityList(List<Object[]> updatedEntityList) {
        this.updatedEntityList = updatedEntityList;
    }
}
