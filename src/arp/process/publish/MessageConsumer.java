package arp.process.publish;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MessageConsumer {

	private Thread receiveThread;
	private Map<String, List<MessageProcessor>> processors = new ConcurrentHashMap<>();
	private Set<String> messageProcessorTypes = new HashSet<>();
	private ExecutorService executorService;

	public MessageConsumer() {
		executorService = Executors.newCachedThreadPool();
	}

	public void registerProcessor(String processDesc, MessageProcessor processor) {
		if (messageProcessorTypes.contains(processor.getClass().getName())) {
			return;
		}
		List<MessageProcessor> list = processors.get(processDesc);
		if (list == null) {
			list = new ArrayList<>();
			processors.put(processDesc, list);
		}
		list.add(processor);
		messageProcessorTypes.add(processor.getClass().getName());
	}

	public void start(List<String> processesToSubscribe, MessageReceiver receiver) {
		receiver.subscribeProcesses(processesToSubscribe);
		receiveThread = new Thread(() -> {
			while (true) {
				List<Message> msgList = null;
				try {
					msgList = receiver.receive();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				if (msgList == null) {
					continue;
				}
				for (Message msg : msgList) {
					List<MessageProcessor> list = processors.get(msg.getProcessDesc());
					if (list == null) {
						continue;
					}
					for (MessageProcessor processor : list) {
						executorService.submit(() -> {
							try {
								processor.process(msg.getProcessOutput());
							} catch (Exception e) {
								e.printStackTrace();
							}
						});
					}
				}
			}
		});
		receiveThread.start();
	}

}
