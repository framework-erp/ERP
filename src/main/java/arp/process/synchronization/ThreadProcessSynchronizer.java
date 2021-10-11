package arp.process.synchronization;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import arp.ARP;
import arp.process.ProcessContext;
import arp.process.ThreadBoundProcessContextArray;

public class ThreadProcessSynchronizer {
	private static String nodeId = "";
	private static Map<String, String> registeredProcessors = new ConcurrentHashMap<>();

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
			processContext.addContextParameter("tid",
					((Long) (Thread.currentThread().getId())).intValue());
			processContext.addContextParameter("nodeId", nodeId);
		}
		ThreadBoundProcessSyncReqFlgArray.setFlg((byte) 1);
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
		registeredProcessors.put(waitingProcessName, waitingProcessName);
	}

}
