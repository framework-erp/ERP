package arp.process.synchronization;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import arp.ARP;
import arp.process.ProcessContext;
import arp.process.ThreadBoundProcessContextArray;

public class ThreadProcessSynchronizer {
	private static String nodeId = "";
	private static Map<String, String> registeredProcessors = new ConcurrentHashMap<>();
	private static long defaultWaitNano = 100 * 1000000l;

	public static void setNodeId(String nodeId) {
		ThreadProcessSynchronizer.nodeId = nodeId;
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
		if (!registeredProcessors.containsKey(waitingProcessName)) {
			registerProcessor(waitingProcessName);
		}
	}

	private synchronized static void registerProcessor(String waitingProcessName) {
		if (registeredProcessors.containsKey(waitingProcessName)) {
			return;
		}
		ARP.registerMessageProcessor(waitingProcessName,
				new ThreadSynchronizerMessageProcessor());
		ARP.subscribeProcess(waitingProcessName);
		registeredProcessors.put(waitingProcessName, waitingProcessName);
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
		} while (true);
	}

}
