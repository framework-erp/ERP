package erp.process.definition;

public class TypedResult {
    private String type;
    private Object result;

    public TypedResult() {
    }

    public TypedResult(String type, Object result) {
        this.type = type;
        this.result = result;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}
