package arp;

import arp.enhance.ClassEnhancer;
import arp.enhance.ClassParseResult;
import arp.process.ProcessContext;
import arp.process.publish.ProcessListenerMessageConsumer;
import arp.process.publish.ProcessListenerMessageProcessor;
import arp.process.publish.ProcessMessageReceiver;
import arp.process.publish.ProcessMessageSender;
import arp.process.publish.ProcessPublisher;

public class ARP {

	private static ProcessListenerMessageConsumer messageConsumer;

	public static void start(String... pkgs) throws Exception {
		ClassParseResult parseResult = ClassEnhancer.parseAndEnhance(pkgs);
		ProcessContext.setProcessInfos(parseResult.getProcessInfoList());
	}

	public static void start(ProcessMessageSender messageSender, String... pkgs)
			throws Exception {
		ClassParseResult parseResult = ClassEnhancer.parseAndEnhance(pkgs);
		ProcessContext.setProcessInfos(parseResult.getProcessInfoList());
		ProcessPublisher.messageSender = messageSender;
	}

	public static void start(ProcessMessageReceiver messageReceiver,
			String... pkgs) throws Exception {
		ClassParseResult parseResult = ClassEnhancer.parseAndEnhance(pkgs);
		ProcessContext.setProcessInfos(parseResult.getProcessInfoList());
		messageConsumer = new ProcessListenerMessageConsumer(messageReceiver);
		messageConsumer.start();
	}

	public static void start(ProcessMessageSender messageSender,
			ProcessMessageReceiver messageReceiver, String... pkgs)
			throws Exception {
		ClassParseResult parseResult = ClassEnhancer.parseAndEnhance(pkgs);
		ProcessContext.setProcessInfos(parseResult.getProcessInfoList());
		ProcessPublisher.messageSender = messageSender;
		messageConsumer = new ProcessListenerMessageConsumer(messageReceiver);
		messageConsumer.start();
	}

	public static void registerMessageProcessor(String processDesc,
			ProcessListenerMessageProcessor processor) {
		messageConsumer.registerProcessor(processDesc, processor);
	}

}
