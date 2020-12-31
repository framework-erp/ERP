package arp.process.publish;

public interface MessageSender {

	void send(Message msg) throws Exception;

}
