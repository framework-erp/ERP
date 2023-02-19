package erp.process.definition.json;

public class TypedEntityUpdateJson {
    private String type;
    private String originalEntityJson;
    private String updatedEntityJson;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOriginalEntityJson() {
        return originalEntityJson;
    }

    public void setOriginalEntityJson(String originalEntityJson) {
        this.originalEntityJson = originalEntityJson;
    }

    public String getUpdatedEntityJson() {
        return updatedEntityJson;
    }

    public void setUpdatedEntityJson(String updatedEntityJson) {
        this.updatedEntityJson = updatedEntityJson;
    }
}
