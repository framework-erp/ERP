package arp.process.monitor;

import arp.process.publish.Message;

public interface ProcessesMonitorMessageProcessor {

	void process(Message message);

}
