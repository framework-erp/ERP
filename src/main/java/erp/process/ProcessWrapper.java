package erp.process;


import erp.AppContext;

import java.util.List;

public class ProcessWrapper {

    public static void beforeProcessStart(String processName) {
        ProcessContext processContext = ThreadBoundProcessContextArray.getProcessContext();
        processContext.startProcess(processName);
        List<ProcessListener> processListeners = AppContext.getProcessListeners();
        for (ProcessListener processListener : processListeners) {
            processListener.beforeProcessStart(processName);
        }
    }

    public static void afterProcessFinish() {
        ProcessContext processContext = ThreadBoundProcessContextArray.getProcessContext();
        try {
            processContext.finishProcess();
            List<ProcessListener> processListeners = AppContext.getProcessListeners();
            for (ProcessListener processListener : processListeners) {
                processListener.afterProcessFinish(processContext.getProcessName());
            }
        } catch (Exception e) {
            throw new RuntimeException("finish process faild", e);
        } finally {
            try {
                processContext.releaseProcessEntities();
            } catch (Exception e) {
                throw new RuntimeException("release process entities faild", e);
            } finally {
                processContext.setStarted(false);
            }
        }
    }

    public static void afterProcessFailed() {
        ProcessContext processContext = ThreadBoundProcessContextArray.getProcessContext();
        processContext.setStarted(false);
        try {
            List<ProcessListener> processListeners = AppContext.getProcessListeners();
            for (ProcessListener processListener : processListeners) {
                processListener.afterProcessFailed(processContext.getProcessName());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                processContext.releaseProcessEntities();
            } catch (Exception e) {
                throw new RuntimeException("release process entities faild", e);
            }
        }
    }

    public static void recordProcessResult(Object result) {
        ProcessContext processContext = ThreadBoundProcessContextArray.getProcessContext();
        processContext.recordProcessResult(result);
    }

    public static void recordProcessArgument(Object argument) {
        ProcessContext processContext = ThreadBoundProcessContextArray.getProcessContext();
        processContext.recordProcessArgument(argument);
    }

}
