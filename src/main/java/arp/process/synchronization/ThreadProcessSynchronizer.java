package arp.process.synchronization;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import arp.process.ProcessContext;
import arp.process.ThreadBoundProcessContextArray;
import arp.process.publish.ProcessMessageReceiver;

public class ThreadProcessSynchronizer {
	private static String nodeId = "";
	private static Map<String, String> subscribedProcessors = new ConcurrentHashMap<>();
	private static long defaultWaitNano = 100 * 1000000l;
	private static ThreadProcessSyncFinishMessageConsumer threadProcessSyncFinishMessageConsumer;

	public static void start(String nodeId, ProcessMessageReceiver receiver) {
		ThreadProcessSynchronizer.nodeId = nodeId;
		threadProcessSyncFinishMessageConsumer = new ThreadProcessSyncFinishMessageConsumer(
				receiver);
		threadProcessSyncFinishMessageConsumer.start();
	}

	public static String getNodeId() {
		return nodeId;
	}

	public static void requestSync(String waitingProcessName) {
		ProcessContext processContext = ThreadBoundProcessContextArray
				.getProcessContext();
		Object tid = processContext.getContextParameter("tid");
		if (tid == null) {
			tid = ((Long) (Thread.currentThread().getId())).intValue();
			processContext.addContextParameter("tid", tid);
			processContext.addContextParameter("nodeId", nodeId);
		}
		ThreadBoundProcessSyncReqFlgArray.setFlg((int) tid, (byte) 1);
		if (!subscribedProcessors.containsKey(waitingProcessName)) {
			subscribeProcess(waitingProcessName);
		}
	}

	private synchronized static void subscribeProcess(String waitingProcessName) {
		if (subscribedProcessors.containsKey(waitingProcessName)) {
			return;
		}
		threadProcessSyncFinishMessageConsumer
				.subscribeProcess(waitingProcessName);
		subscribedProcessors.put(waitingProcessName, waitingProcessName);
	}

	public static void threadWait() {
		threadWaitNano(defaultWaitNano);
	}

	public static void threadWait(long waitTime) {
		threadWaitNano(waitTime * 1000000);
	}

	private static void threadWaitNano(long waitNano) {
		long startTime = System.nanoTime();
		int tid = (int) Thread.currentThread().getId();
		do {
			byte flg = ThreadBoundProcessSyncReqFlgArray.getFlg(tid);
			if (flg == 0) {
				return;
			}
			if ((System.nanoTime() - startTime) > waitNano) {
				return;
			}
			Thread.yield();
		} while (true);
	}

}
