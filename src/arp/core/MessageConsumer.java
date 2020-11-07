package arp.core;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MessageConsumer {

	private Thread receiveThread;
	private MessageReceiver receiver;
	private MessageProcessor processor;
	private ExecutorService executorService;

	public MessageConsumer() {
		executorService = Executors.newCachedThreadPool();
	}

	public void start() {
		receiveThread = new Thread(() -> {
			while (true) {
				List<Message> msgList = receiver.receive();
				for (Message msg : msgList) {
					executorService.submit(() -> {
						processor.process(msg);
					});
				}
			}
		});
		receiveThread.start();
	}

}
