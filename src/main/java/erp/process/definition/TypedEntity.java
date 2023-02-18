package erp.process.definition;

public class TypedEntity {
    private String type;
    private Object entity;

    public TypedEntity() {
    }

    public TypedEntity(String type, Object entity) {
        this.type = type;
        this.entity = entity;
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
}
