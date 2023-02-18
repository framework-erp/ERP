package erp.process.definition;

public class TypedEntityUpdate {
    private String type;
    private Object originalEntity;
    private Object updatedEntity;

    public TypedEntityUpdate() {
    }

    public TypedEntityUpdate(String type, Object originalEntity, Object updatedEntity) {
        this.type = type;
        this.originalEntity = originalEntity;
        this.updatedEntity = updatedEntity;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getOriginalEntity() {
        return originalEntity;
    }

    public void setOriginalEntity(Object originalEntity) {
        this.originalEntity = originalEntity;
    }

    public Object getUpdatedEntity() {
        return updatedEntity;
    }

    public void setUpdatedEntity(Object updatedEntity) {
        this.updatedEntity = updatedEntity;
    }
}
