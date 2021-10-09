package arp.process.synchronization;

import arp.process.ProcessContext;
import arp.process.ThreadBoundProcessContextArray;

public class ThreadProcessSynchronizer {
	private static String nodeId = "";

	public static void setNodeId(String nodeId) {
		ThreadProcessSynchronizer.nodeId = nodeId;
	}

	public static void requestSync(String waitingProcessName) {
		ProcessContext processContext = ThreadBoundProcessContextArray
				.getProcessContext();
	}

}
