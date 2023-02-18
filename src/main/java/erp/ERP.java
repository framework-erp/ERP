package erp;

import erp.annotation.ProcessEnhancer;
import erp.process.ProcessContext;
import erp.process.ProcessWrapper;
import erp.process.ThreadBoundProcessContextArray;
import erp.process.definition.Process;

import java.util.concurrent.Callable;

public class ERP {
    public static void useAnnotation() {
        ProcessEnhancer.scanAndEnhanceProcess();
    }


    public static <V> V go(String processName, Callable<V> process) {
        ProcessWrapper.beforeProcessStart(processName);
        try {
            V v = process.call();
            ProcessWrapper.afterProcessFinish();
            return v;
        } catch (Exception e) {
            ProcessWrapper.afterProcessFailed();
            throw new RuntimeException(e);
        }
    }

    public static Process getProcess() {
        ProcessContext processContext = ThreadBoundProcessContextArray.getProcessContext();
        if (processContext == null) {
            return null;
        }
        return processContext.buildProcess();
    }


}
