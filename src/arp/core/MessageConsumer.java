package arp.core;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MessageConsumer {

	private Thread receiveThread;
	private MessageReceiver receiver;
	private Map<String, Set<MessageProcessor>> processors = new ConcurrentHashMap<>();
	private ExecutorService executorService;

	public MessageConsumer() {
		executorService = Executors.newCachedThreadPool();
	}

	public void registerProcessor(String processDesc, MessageProcessor processor) {
		Set<MessageProcessor> set = processors.get(processDesc);
		if (set == null) {
			set = new HashSet<>();
			processors.put(processDesc, set);
		}
		set.add(processor);
	}

	public void start() {
		receiveThread = new Thread(() -> {
			while (true) {
				List<Message> msgList = receiver.receive();
				for (Message msg : msgList) {
					Set<MessageProcessor> set = processors.get(msg.getProcessDesc());
					if (set == null) {
						continue;
					}
					for (MessageProcessor processor : set) {
						executorService.submit(() -> {
							processor.process(msg.getProcessOutput());
						});
					}
				}
			}
		});
		receiveThread.start();
	}

}
