package erp.process;

import java.util.List;
import java.util.Map;

/**
 * 完整描述过程的标准数据结构
 */
public class Process {
    private String name;
    private List<Object> argumentList;
    private Object result;
    private List<Map> createdEntityList;
    private List<Map> deletedEntityList;
    private List<Map> updatedEntityList;

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

    public List<Map> getCreatedEntityList() {
        return createdEntityList;
    }

    public void setCreatedEntityList(List<Map> createdEntityList) {
        this.createdEntityList = createdEntityList;
    }

    public List<Map> getDeletedEntityList() {
        return deletedEntityList;
    }

    public void setDeletedEntityList(List<Map> deletedEntityList) {
        this.deletedEntityList = deletedEntityList;
    }

    public List<Map> getUpdatedEntityList() {
        return updatedEntityList;
    }

    public void setUpdatedEntityList(List<Map> updatedEntityList) {
        this.updatedEntityList = updatedEntityList;
    }
}
