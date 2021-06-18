package arp.process.publish;

import java.util.List;

public class ProcessPublisher {

	public static MessageSender messageSender;

	public static void publish(List<Object> arguments, Object processResult,
			String processDesc, boolean dontPublishWhenResultIsNull) {
		if (dontPublishWhenResultIsNull && processResult == null) {
			return;
		}
		Message msg = new Message();
		msg.setProcessDesc(processDesc);
		msg.setProcessInput(arguments);
		msg.setProcessOutput(processResult);
		try {
			messageSender.send(msg);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
