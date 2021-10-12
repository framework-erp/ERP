package arp.process.synchronization;

import arp.util.Unsafe;

public class ThreadBoundProcessSyncReqFlgArray {
	private static byte[] threadProcessSyncReqFlgArray = new byte[1024];

	public static void setFlg(int tid, byte flg) {
		if (tid >= threadProcessSyncReqFlgArray.length) {
			resizeArray(tid);
		}
		Unsafe.setVolatile(threadProcessSyncReqFlgArray, tid, flg);
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

	public static byte getFlg(int tid) {
		if (tid >= threadProcessSyncReqFlgArray.length) {
			resizeArray(tid);
		}
		return Unsafe.getVolatile(threadProcessSyncReqFlgArray, tid);
	}

}
