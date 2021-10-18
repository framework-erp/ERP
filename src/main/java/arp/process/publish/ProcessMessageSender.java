package arp.process.publish;

import java.util.List;

public interface ProcessMessageSender {

	/**
	 * 具体实现要保证方法立即返回
	 */
	void send(String processDesc, Message msg) throws Exception;

	void defineProcessesToSend(List<String> processesToSend);

}
