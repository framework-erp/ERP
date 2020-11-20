package arp;

import arp.core.ClassEnhancer;
import arp.core.MessageConsumer;
import arp.core.MessageProcessor;
import arp.core.MessageReceiver;
import arp.core.MessageSender;
import arp.core.ProcessPublisher;

public class ARP {

	private static MessageConsumer messageConsumer;

	public static void start(String... pkgs) throws Exception {
		ClassEnhancer.enhance(pkgs);
	}

	public static void start(MessageSender messageSender, String... pkgs) throws Exception {
		ClassEnhancer.enhance(pkgs);
		ProcessPublisher.messageSender = messageSender;
	}

	public static void start(MessageReceiver messageReceiver, String... pkgs) throws Exception {
		ClassEnhancer.enhance(pkgs);
		messageConsumer = new MessageConsumer();
		messageConsumer.start(messageReceiver);
	}

	public static void start(MessageSender messageSender, MessageReceiver messageReceiver, String... pkgs)
			throws Exception {
		ClassEnhancer.enhance(pkgs);
		ProcessPublisher.messageSender = messageSender;
		messageConsumer = new MessageConsumer();
		messageConsumer.start(messageReceiver);
	}

	public static void registerMessageProcessor(String processDesc, MessageProcessor processor) {
		messageConsumer.registerProcessor(processDesc, processor);
	}

}
