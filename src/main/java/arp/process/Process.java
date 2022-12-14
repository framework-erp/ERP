package arp.process;

import java.util.ArrayList;
import java.util.List;

/**
 * 完整描述过程的标准数据结构
 */
public class Process {
    private List<Object> arguments;
    private Object result;
    private List<Object> createdAggrs;
    private List<Object> deletedAggrs;
    private List<Object[]> updatedAggrs;
    private String name;

    public List<Object> getArguments() {
        return arguments;
    }

    public void setArguments(List<Object> arguments) {
        this.arguments = arguments;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public List<Object> getCreatedAggrs() {
        return createdAggrs;
    }

    public void setCreatedAggrs(List<Object> createdAggrs) {
        this.createdAggrs = createdAggrs;
    }

    public List<Object> getDeletedAggrs() {
        return deletedAggrs;
    }

    public void setDeletedAggrs(List<Object> deletedAggrs) {
        this.deletedAggrs = deletedAggrs;
    }

    public List<Object[]> getUpdatedAggrs() {
        return updatedAggrs;
    }

    public void setUpdatedAggrs(List<Object[]> updatedAggrs) {
        this.updatedAggrs = updatedAggrs;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
