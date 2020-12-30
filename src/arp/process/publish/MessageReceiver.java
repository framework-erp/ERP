package arp.process.publish;

import java.util.List;

public interface MessageReceiver {

	List<Message> receive();

	void subscribeProcesses(List<String> processesToSubscribe);

}
