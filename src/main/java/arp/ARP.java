package arp;

import java.util.ArrayList;
import java.util.List;

import arp.enhance.ClassEnhancer;
import arp.enhance.ClassParseResult;
import arp.enhance.ListenerInfo;
import arp.enhance.ProcessInfo;
import arp.process.ProcessContext;
import arp.process.monitor.MonitorMessageConvertor;
import arp.process.publish.Message;
import arp.process.publish.MessageReceiver;
import arp.process.publish.MessageSender;
import arp.process.publish.ProcessListenerMessageConsumer;
import arp.process.publish.ProcessListenerMessageProcessor;
import arp.process.publish.ProcessPublisher;

public class ARP {

	private static ProcessListenerMessageConsumer messageConsumer;

	public static void start(String... pkgs) throws Exception {
		ClassParseResult parseResult = ClassEnhancer.parseAndEnhance(pkgs);
		ProcessContext.setProcessInfos(parseResult.getProcessInfoList());
	}

	public static void start(MessageSender<Message> messageSender,
			String... pkgs) throws Exception {
		ClassParseResult parseResult = ClassEnhancer.parseAndEnhance(pkgs);
		ProcessContext.setProcessInfos(parseResult.getProcessInfoList());
		List<String> processesToPublish = getProcessesToSend(parseResult);
		ProcessPublisher.messageSender = messageSender;
		ProcessPublisher.defineProcessesToPublish(processesToPublish);
	}

	public static void start(MessageReceiver<Message> messageReceiver,
			String... pkgs) throws Exception {
		ClassParseResult parseResult = ClassEnhancer.parseAndEnhance(pkgs);
		ProcessContext.setProcessInfos(parseResult.getProcessInfoList());
		List<String> processesToSubscribe = getProcessesToSubscribe(parseResult);
		messageConsumer = new ProcessListenerMessageConsumer(messageReceiver);
		for (String processToSubscribe : processesToSubscribe) {
			messageConsumer.subscribeProcess(processToSubscribe);
		}
		messageConsumer.start();
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

	public static void start(MessageSender<Message> messageSender,
			MessageReceiver<Message> messageReceiver, String... pkgs)
			throws Exception {
		ClassParseResult parseResult = ClassEnhancer.parseAndEnhance(pkgs);
		ProcessContext.setProcessInfos(parseResult.getProcessInfoList());
		List<String> processesToSubscribe = getProcessesToSubscribe(parseResult);
		List<String> processesToPublish = getProcessesToSend(parseResult);
		ProcessPublisher.messageSender = messageSender;
		ProcessPublisher.defineProcessesToPublish(processesToPublish);
		messageConsumer = new ProcessListenerMessageConsumer(messageReceiver);
		for (String processToSubscribe : processesToSubscribe) {
			messageConsumer.subscribeProcess(processToSubscribe);
		}
		messageConsumer.start();
	}

	private static List<String> getProcessesToSend(ClassParseResult parseResult) {
		if (parseResult == null) {
			return null;
		}
		List<ProcessInfo> processInfoList = parseResult.getProcessInfoList();
		List<String> processesToSend = new ArrayList<>();
		for (ProcessInfo processInfo : processInfoList) {
			String processDesc;
			if (!processInfo.getProcessName().trim().isEmpty()) {
				processDesc = processInfo.getProcessName();
			} else {
				processDesc = processInfo.getClsName() + "."
						+ processInfo.getMthName();
			}
			processesToSend.add(processDesc);
		}
		return processesToSend;
	}

	public static void registerMessageProcessor(String processDesc,
			ProcessListenerMessageProcessor processor) {
		messageConsumer.registerProcessor(processDesc, processor);
	}

	public static void startJoinMonitor(
			MonitorMessageConvertor monitorMessageConvertor) throws Exception {
		ClassParseResult parseResult = ClassEnhancer.parseResult;
		List<String> processesToPublish = getProcessesToSend(parseResult);
		monitorMessageConvertor.start(processesToPublish);
	}

}
