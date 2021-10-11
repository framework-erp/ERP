package arp.process.synchronization;

import java.util.Map;

import arp.process.publish.Message;
import arp.process.publish.ProcessListenerMessageProcessor;

public class ThreadSynchronizerMessageProcessor implements
		ProcessListenerMessageProcessor {

	@Override
	public void process(Message msg) {
		Map<String, Object> contextParameters = msg.getContextParameters();
		if (contextParameters == null) {
			return;
		}
		if (!ThreadProcessSynchronizer.getNodeId().equals(
				contextParameters.get("nodeId"))) {
			return;
		}
		int tid = (int) contextParameters.get("tid");
		if (ThreadBoundProcessSyncReqFlgArray.getFlg(tid) == 1) {
			ThreadBoundProcessSyncReqFlgArray.setFlg((byte) 0);
		}
	}
}
