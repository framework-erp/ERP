package arp.core;

public class ARP {

	private static MessageConsumer messageConsumer;

	public static void startMessageConsumer() {
		messageConsumer = new MessageConsumer();
	}

	public static void registerMessageProcessor(String processDesc, MessageProcessor processor) {
		messageConsumer.registerProcessor(processDesc, processor);
	}

}
