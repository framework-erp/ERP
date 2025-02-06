package erp;

import erp.annotation.ProcessEnhancer;
import erp.process.ProcessContext;
import erp.process.ProcessWrapper;
import erp.process.ThreadBoundProcessContextArray;
import erp.process.definition.Process;
import erp.repository.TakeEntityException;

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

    public static <T> RetryResult<T> retry(Callable<T> process, int tryTimesForTakeEntityException, long sleepTime) {
        int triedTimes = 0;
        while (triedTimes < tryTimesForTakeEntityException) {
            try {
                T processReturnValue = process.call();
                triedTimes++;
                RetryResult<T> retryResult = new RetryResult<>();
                retryResult.setTriedTimes(triedTimes);
                retryResult.setProcessReturnValue(processReturnValue);
                return retryResult;
            } catch (Exception e) {
                if (e instanceof TakeEntityException) {
                    if (triedTimes == tryTimesForTakeEntityException - 1) {
                        throw (TakeEntityException) e;
                    }
                    triedTimes++;
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                } else {
                    if (RuntimeException.class.isAssignableFrom(e.getClass())) {
                        throw (RuntimeException) e;
                    }
                    RetryResult<T> retryResult = new RetryResult<>();
                    retryResult.setTriedTimes(triedTimes);
                    retryResult.setException(e);
                    return retryResult;
                }
            }
        }
        throw new RuntimeException("Should not reach here");
    }

    public static void retry(Runnable process, int tryTimesForTakeEntityException, long sleepTime) {
        retry(() -> {
            process.run();
            return null;
        }, tryTimesForTakeEntityException, sleepTime);
    }


}
