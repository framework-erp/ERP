package erp.process;


public class ThreadBoundProcessContextArray {
    private static ProcessContext[] threadProcessContextArray;

    static {
        threadProcessContextArray = new ProcessContext[1024];
        for (int i = 0; i < threadProcessContextArray.length; i++) {
            threadProcessContextArray[i] = new ProcessContext();
        }
    }

    public static ProcessContext getProcessContext() {
        int iTid = (int) Thread.currentThread().getId();
        if (iTid >= threadProcessContextArray.length) {
            resizeArray(iTid);
        }
        return threadProcessContextArray[iTid];
    }

    private static synchronized void resizeArray(int iTid) {
        if (iTid < threadProcessContextArray.length) {
            return;
        }
        ProcessContext[] newArray = new ProcessContext[iTid * 2];
        System.arraycopy(threadProcessContextArray, 0, newArray, 0,
                threadProcessContextArray.length);
        for (int i = threadProcessContextArray.length; i < newArray.length; i++) {
            newArray[i] = new ProcessContext();
        }
        threadProcessContextArray = newArray;
    }

}
