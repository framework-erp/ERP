package arp.process.publish;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class ProcessesMonitor {
	private Thread receiveThread;
	private ExecutorService executorService;
	private ProcessesMonitorMessageProcessor processor;

	public ProcessesMonitor() {
		executorService = Executors.newCachedThreadPool();
	}

	public void setProcessor(ProcessesMonitorMessageProcessor processor) {
		this.processor = processor;
	}

	public void updateAllProcessesToSubscribe() {
		List<String> processesToSubscribe = queryAllProcessesToSubscribe();
		subscribeProcesses(processesToSubscribe);
	}

	protected abstract void subscribeProcesses(List<String> processesToSubscribe);

	protected abstract List<String> queryAllProcessesToSubscribe();

	protected abstract List<Message> receive() throws Exception;

	public void start() {
		updateAllProcessesToSubscribe();
		receiveThread = new Thread(() -> {
			while (true) {
				List<Message> msgList = null;
				try {
					msgList = receive();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			if (msgList == null) {
				continue;
			}
			for (Message msg : msgList) {
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
	}	);
		receiveThread.start();
	}
}
