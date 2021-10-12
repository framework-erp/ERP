package arp.process.publish;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProcessListenerMessageConsumer {

	private Map<String, List<ProcessListenerMessageProcessor>> processors = new ConcurrentHashMap<>();
	private ProcessListenerMessageReceiver receiver;
	private ExecutorService executorService;

	public ProcessListenerMessageConsumer() {
		executorService = Executors.newCachedThreadPool();
	}

	public void registerProcessor(String processDesc,
			ProcessListenerMessageProcessor processor) {
		List<ProcessListenerMessageProcessor> list = processors
				.get(processDesc);
		if (list == null) {
			list = new ArrayList<>();
			processors.put(processDesc, list);
		}
		list.add(processor);
	}

	public void subscribeProcess(String processDesc) {
		receiver.subscribeProcess(processDesc);
	}

	public void start(List<String> processesToSubscribe,
			ProcessListenerMessageReceiver receiver) {
		this.receiver = receiver;
		receiver.subscribeProcesses(processesToSubscribe);
		new Thread(() -> {
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
				List<ProcessListenerMessageProcessor> list = processors.get(msg
						.getProcessDesc());
				if (list == null) {
					continue;
				}
				for (ProcessListenerMessageProcessor processor : list) {
					executorService.submit(() -> {
						try {
							processor.process(msg);
						} catch (Exception e) {
							e.printStackTrace();
						}
					});
				}
			}
		}
	}	).start();
	}

}
