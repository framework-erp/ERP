package erp.process;


import erp.AppContext;

import java.util.List;

public class ProcessWrapper {

    public static void beforeProcessStart(String processName) {
        ProcessContext processContext = ThreadBoundProcessContextArray.createProcessContext();
        processContext.startProcess(processName);
        List<ProcessListener> processListeners = AppContext.getProcessListeners();
        for (ProcessListener processListener : processListeners) {
            processListener.beforeProcessStart(processName);
        }
    }

    public static void afterProcessFinish() {
        ProcessContext processContext = ThreadBoundProcessContextArray.getProcessContext();
        processContext.finishProcess();
        List<ProcessListener> processListeners = AppContext.getProcessListeners();
        for (ProcessListener processListener : processListeners) {
            processListener.afterProcessFinish(processContext.getProcessName());
        }
    }

    public static void afterProcessFailed() {
        ProcessContext processContext = ThreadBoundProcessContextArray.getProcessContext();
        processContext.processFaild();
        List<ProcessListener> processListeners = AppContext.getProcessListeners();
        for (ProcessListener processListener : processListeners) {
            processListener.afterProcessFailed(processContext.getProcessName());
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
