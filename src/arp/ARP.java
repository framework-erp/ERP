package arp;

import arp.enhance.ClassEnhancer;
import arp.process.publish.MessageConsumer;
import arp.process.publish.MessageProcessor;
import arp.process.publish.MessageReceiver;
import arp.process.publish.MessageSender;
import arp.process.publish.ProcessPublisher;

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
