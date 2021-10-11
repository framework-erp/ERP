package arp.process.synchronization;

import arp.util.Unsafe;

public class ThreadBoundProcessSyncReqFlgArray {
	private static byte[] threadProcessSyncReqFlgArray = new byte[1024];

	public static void setFlg(byte flg) {
		int iTid = (int) Thread.currentThread().getId();
		if (iTid >= threadProcessSyncReqFlgArray.length) {
			resizeArray(iTid);
		}
		Unsafe.setVolatile(threadProcessSyncReqFlgArray, iTid, flg);
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
