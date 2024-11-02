package erp.process.definition;

public class TypedEntity {
    private String type;
    private Object entity;
    private String repositoryName;

    public TypedEntity() {
    }

    public TypedEntity(Object entity, String repositoryName) {
        this.type = entity.getClass().getName();
        this.entity = entity;
        this.repositoryName = repositoryName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getEntity() {
        return entity;
    }

    public void setEntity(Object entity) {
        this.entity = entity;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }
}
