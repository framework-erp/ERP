package erp;

import erp.annotation.ProcessEnhancer;
import erp.process.ProcessWrapper;

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


}
