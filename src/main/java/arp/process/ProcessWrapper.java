package arp.process;


public class ProcessWrapper {

    public static void beforeProcessStart(String processName) {
        ProcessContext processContext = ThreadBoundProcessContextArray.createProcessContext();
        processContext.startProcess(processName);
    }

    public static void afterProcessFinish() {
        ProcessContext processContext = ThreadBoundProcessContextArray.getProcessContext();
        processContext.finishProcess();
    }

    public static void afterProcessFaild() {
        ProcessContext processContext = ThreadBoundProcessContextArray.getProcessContext();
        processContext.processFaild();
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
