package arp.process.publish;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class ProcessesMonitor {
	private ExecutorService executorService;
	private ProcessesMonitorMessageProcessor processor;
	private Runnable subscribeProcessesTask;

	public ProcessesMonitor() {
		executorService = Executors.newCachedThreadPool();
	}

	public void setProcessor(ProcessesMonitorMessageProcessor processor) {
		this.processor = processor;
	}

	public void updateAllProcessesToSubscribe() {
		List<String> processesToSubscribe = queryAllProcessesToSubscribe();
		subscribeProcessesTask = () -> {
			subscribeProcesses(processesToSubscribe);
		};
	}

	protected abstract void subscribeProcesses(List<String> processesToSubscribe);

	protected abstract List<String> queryAllProcessesToSubscribe();

	protected abstract List<MonitorMessage> receive() throws Exception;

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
					msgList = receive();
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
