package arp.process.publish;

public interface ProcessMessageSender {

	/**
	 * 具体实现要保证方法立即返回
	 */
	void send(Message msg) throws Exception;

}
