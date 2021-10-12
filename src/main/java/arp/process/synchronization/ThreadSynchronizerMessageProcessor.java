package arp.process.synchronization;

import java.util.List;
import java.util.Map;

import arp.process.publish.Message;
import arp.process.publish.ProcessListenerMessageProcessor;

public class ThreadSynchronizerMessageProcessor implements
		ProcessListenerMessageProcessor {

	@Override
	public void process(Message msg) {
		List<Map<String, Object>> contextParametersTrace = msg
				.getContextParametersTrace();
		if (contextParametersTrace == null || contextParametersTrace.isEmpty()) {
			return;
		}
		Map<String, Object> contextParameters = contextParametersTrace.get(0);
		if (!ThreadProcessSynchronizer.getNodeId().equals(
				contextParameters.get("nodeId"))) {
			return;
		}
		int tid = (int) contextParameters.get("tid");
		if (ThreadBoundProcessSyncReqFlgArray.getFlg(tid) == 1) {
			ThreadBoundProcessSyncReqFlgArray.setFlg(tid, (byte) 0);
		}
	}
}
