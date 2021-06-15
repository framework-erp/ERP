package arp.process.publish;

public class ProcessPublisher {

	public static MessageSender messageSender;

	public static void publish(Object processResult, String processDesc,
			boolean dontPublishWhenResultIsNull) {
		if (dontPublishWhenResultIsNull && processResult == null) {
			return;
		}
		Message msg = new Message();
		msg.setProcessDesc(processDesc);
		msg.setProcessOutput(processResult);
		try {
			messageSender.send(msg);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
