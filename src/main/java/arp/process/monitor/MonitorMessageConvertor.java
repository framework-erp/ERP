package arp.process.monitor;

import java.util.List;

import arp.process.publish.Message;

public abstract class MonitorMessageConvertor {

	public void start(List<String> processesToPublish) {
		defineProcessesToPublish(processesToPublish);
		subscribeProcesses(processesToPublish);
		new Thread(() -> {
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
				MonitorMessage monitorMessage = convertMessage(msg);
				try {
					send(monitorMessage);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}	).start();
	}

	protected abstract void defineProcessesToPublish(
			List<String> processesToPublish);

	protected abstract void send(MonitorMessage monitorMessage)
			throws Exception;

	protected abstract MonitorMessage convertMessage(Message msg);

	protected abstract List<Message> receive() throws Exception;

	protected abstract void subscribeProcesses(List<String> processesToSubscribe);

}
