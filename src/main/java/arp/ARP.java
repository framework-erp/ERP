package arp;

import java.util.ArrayList;
import java.util.List;

import arp.enhance.ClassEnhancer;
import arp.enhance.ClassParseResult;
import arp.enhance.ListenerInfo;
import arp.enhance.ProcessInfo;
import arp.process.publish.MessageSender;
import arp.process.publish.ProcessListenerMessageConsumer;
import arp.process.publish.ProcessListenerMessageProcessor;
import arp.process.publish.ProcessListenerMessageReceiver;
import arp.process.publish.ProcessPublisher;

public class ARP {

	private static ProcessListenerMessageConsumer messageConsumer;

	public static void start(String... pkgs) throws Exception {
		ClassEnhancer.parseAndEnhance(pkgs);
	}

	public static void start(MessageSender messageSender, String... pkgs)
			throws Exception {
		ClassEnhancer.parseAndEnhance(pkgs);
		ProcessPublisher.messageSender = messageSender;
	}

	public static void start(ProcessListenerMessageReceiver messageReceiver,
			String... pkgs) throws Exception {
		ClassParseResult parseResult = ClassEnhancer.parseAndEnhance(pkgs);
		List<String> processesToSubscribe = getProcessesToSubscribe(parseResult);
		messageConsumer = new ProcessListenerMessageConsumer();
		messageConsumer.start(processesToSubscribe, messageReceiver);
	}

	private static List<String> getProcessesToSubscribe(
			ClassParseResult parseResult) {
		if (parseResult == null) {
			return null;
		}
		List<ProcessInfo> processInfoList = parseResult.getProcessInfoList();
		List<String> processesToSubscribe = new ArrayList<>();
		for (ProcessInfo processInfo : processInfoList) {
			ListenerInfo listenerInfo = processInfo.getListenerInfo();
			if (listenerInfo == null) {
				continue;
			}
			String processDesc = listenerInfo.getProcessDesc();
			processesToSubscribe.add(processDesc);
		}
		return processesToSubscribe;
	}

	public static void start(MessageSender messageSender,
			ProcessListenerMessageReceiver messageReceiver, String... pkgs)
			throws Exception {
		ClassParseResult parseResult = ClassEnhancer.parseAndEnhance(pkgs);
		List<String> processesToSubscribe = getProcessesToSubscribe(parseResult);
		ProcessPublisher.messageSender = messageSender;
		messageConsumer = new ProcessListenerMessageConsumer();
		messageConsumer.start(processesToSubscribe, messageReceiver);
	}

	public static void registerMessageProcessor(String processDesc,
			ProcessListenerMessageProcessor processor) {
		messageConsumer.registerProcessor(processDesc, processor);
	}

}
