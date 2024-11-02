package erp.process.definition;

public class TypedEntityUpdate {
    private String type;
    private Object originalEntity;
    private Object updatedEntity;
    private String repositoryName;

    public TypedEntityUpdate() {
    }

    public TypedEntityUpdate(Object originalEntity, Object updatedEntity, String repositoryName) {
        this.type = originalEntity.getClass().getName();
        this.originalEntity = originalEntity;
        this.updatedEntity = updatedEntity;
        this.repositoryName = repositoryName;
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

    public String getRepositoryName() {
        return repositoryName;
    }

    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }
}
