package arp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import arp.enhance.ClassEnhancer;
import arp.enhance.ClassParseResult;
import arp.process.publish.ProcessListenerMessageConsumer;
import arp.process.publish.ProcessListenerMessageProcessor;
import arp.process.publish.ProcessListenerMessageReceiver;
import arp.process.publish.MessageSender;
import arp.process.publish.ProcessPublisher;

public class ARP {

	private static ProcessListenerMessageConsumer messageConsumer;

	public static void start(String... pkgs) throws Exception {
		ClassEnhancer.parseAndEnhance(pkgs);
	}

	public static void start(MessageSender messageSender, String... pkgs) throws Exception {
		ClassEnhancer.parseAndEnhance(pkgs);
		ProcessPublisher.messageSender = messageSender;
	}

	public static void start(ProcessListenerMessageReceiver messageReceiver, String... pkgs) throws Exception {
		ClassParseResult parseResult = ClassEnhancer.parseAndEnhance(pkgs);
		List<String> processesToSubscribe = getProcessesToSubscribe(parseResult);
		messageConsumer = new ProcessListenerMessageConsumer();
		messageConsumer.start(processesToSubscribe, messageReceiver);
	}

	private static List<String> getProcessesToSubscribe(ClassParseResult parseResult) {
		if (parseResult == null) {
			return null;
		}
		List<Map<String, Object>> listnersList = parseResult.getListnersList();
		List<String> processesToSubscribe = new ArrayList<>();
		for (Map<String, Object> listner : listnersList) {
			String processDesc = (String) listner.get("processDesc");
			processesToSubscribe.add(processDesc);
		}
		return processesToSubscribe;
	}

	public static void start(MessageSender messageSender, ProcessListenerMessageReceiver messageReceiver, String... pkgs)
			throws Exception {
		ClassParseResult parseResult = ClassEnhancer.parseAndEnhance(pkgs);
		List<String> processesToSubscribe = getProcessesToSubscribe(parseResult);
		ProcessPublisher.messageSender = messageSender;
		messageConsumer = new ProcessListenerMessageConsumer();
		messageConsumer.start(processesToSubscribe, messageReceiver);
	}

	public static void registerMessageProcessor(String processDesc, ProcessListenerMessageProcessor processor) {
		messageConsumer.registerProcessor(processDesc, processor);
	}

}
