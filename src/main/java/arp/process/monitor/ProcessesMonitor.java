package arp.process.monitor;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import arp.process.publish.MessageReceiver;

public class ProcessesMonitor {
	private ExecutorService executorService;
	private ProcessesMonitorMessageProcessor processor;
	private Runnable subscribeProcessesTask;

	private MessageReceiver<MonitorMessage> messageReceiver;

	public ProcessesMonitor(MessageReceiver<MonitorMessage> messageReceiver) {
		this.messageReceiver = messageReceiver;
		executorService = Executors.newCachedThreadPool();
	}

	public void setProcessor(ProcessesMonitorMessageProcessor processor) {
		this.processor = processor;
	}

	public void updateAllProcessesToSubscribe() {
		List<String> processesToSubscribe = messageReceiver
				.queryAllProcessesToSubscribe();
		subscribeProcessesTask = () -> {
			if (processesToSubscribe != null) {
				for (String process : processesToSubscribe) {
					messageReceiver.subscribeProcess(process);
				}
			}
		};
	}

	public void start() {
		updateAllProcessesToSubscribe();
		new Thread(() -> {
			while (true) {
				if (subscribeProcessesTask != null) {
					try {
						subscribeProcessesTask.run();
					} catch (Exception e) {
						e.printStackTrace();
					}
					subscribeProcessesTask = null;
				}
				List<MonitorMessage> msgList = null;
				try {
					msgList = messageReceiver.receive();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				if (msgList == null || msgList.isEmpty()) {
					try {
						Thread.sleep(100);
					} catch (Exception e) {
						e.printStackTrace();
					}
					continue;
				}
				for (MonitorMessage msg : msgList) {
					if (processor == null) {
						continue;
					}
					executorService.submit(() -> {
						try {
							processor.process(msg);
						} catch (Exception e) {
							e.printStackTrace();
						}
					});
				}
			}
		}).start();
	}

}
