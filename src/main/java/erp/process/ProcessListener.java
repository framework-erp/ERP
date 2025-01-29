package erp.process;

public interface ProcessListener {
    void beforeProcessStart(String processName);

    void afterProcessFinish(String processName);

    void afterProcessFailed(String processName);
}
