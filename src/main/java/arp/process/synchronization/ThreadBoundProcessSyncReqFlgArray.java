package arp.process.synchronization;

public class ThreadBoundProcessSyncReqFlgArray {
	private static byte[] threadProcessSyncReqFlgArray = new byte[1024];

	public static void requestSync() {
		int iTid = (int) Thread.currentThread().getId();
		if (iTid >= threadProcessSyncReqFlgArray.length) {
			resizeArray(iTid);
		}
		// TODO unsafe 方式 赋值
	}

	private static synchronized void resizeArray(int iTid) {
		if (iTid < threadProcessSyncReqFlgArray.length) {
			return;
		}
		byte[] newArray = new byte[iTid * 2];
		System.arraycopy(threadProcessSyncReqFlgArray, 0, newArray, 0,
				threadProcessSyncReqFlgArray.length);
		threadProcessSyncReqFlgArray = newArray;
	}

}
