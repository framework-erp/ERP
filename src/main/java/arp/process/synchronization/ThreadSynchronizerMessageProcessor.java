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
	}

}
