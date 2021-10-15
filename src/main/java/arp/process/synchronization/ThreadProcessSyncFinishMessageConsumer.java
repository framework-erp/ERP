package arp.process.synchronization;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import arp.process.publish.Message;
import arp.process.publish.MessageReceiver;

public class ThreadProcessSyncFinishMessageConsumer {

	private MessageReceiver receiver;
	private ExecutorService executorService;

	public ThreadProcessSyncFinishMessageConsumer(
			MessageReceiver receiver) {
		this.receiver = receiver;
		executorService = Executors.newCachedThreadPool();
	}

	public void subscribeProcess(String processDesc) {
		receiver.subscribeProcess(processDesc);
	}

	public void start() {
		new Thread(
				() -> {
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
							executorService
									.submit(() -> {
										List<Map<String, Object>> contextParametersTrace = msg
												.getContextParametersTrace();
										if (contextParametersTrace == null
												|| contextParametersTrace
														.isEmpty()) {
											return;
										}
										Map<String, Object> contextParameters = contextParametersTrace
												.get(0);
										if (!ThreadProcessSynchronizer
												.getNodeId().equals(
														contextParameters
																.get("nodeId"))) {
											return;
										}
										int tid = (int) contextParameters
												.get("tid");
										if (ThreadBoundProcessSyncReqFlgArray
												.getFlg(tid) == 1) {
											ThreadBoundProcessSyncReqFlgArray
													.setFlg(tid, (byte) 0);
										}
									});
						}
					}
				}).start();
	}

}
