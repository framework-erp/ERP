package arp.process;


public class ThreadBoundProcessContextArray {
	private static ProcessContext[] threadProcessContextArray = new ProcessContext[1024];

	public static ProcessContext getProcessContext() {
		int iTid = (int) Thread.currentThread().getId();
		if (iTid >= threadProcessContextArray.length) {
			resizeArray(iTid);
		}
		ProcessContext ctx = threadProcessContextArray[iTid];
		if (ctx == null) {
			ctx = new ProcessContext();
			threadProcessContextArray[iTid] = ctx;
		}
		return ctx;
	}

	private static synchronized void resizeArray(int iTid) {
		if (iTid < threadProcessContextArray.length) {
			return;
		}
		ProcessContext[] newArray = new ProcessContext[iTid * 2];
		System.arraycopy(threadProcessContextArray, 0, newArray, 0,
				threadProcessContextArray.length);
		threadProcessContextArray = newArray;
	}

}
