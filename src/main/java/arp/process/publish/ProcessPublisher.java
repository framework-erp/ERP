package arp.process.publish;

import java.util.List;

public class ProcessPublisher {

	public static MessageSender messageSender;

	public static void publish(List<Object> processArguments,
			Object processResult, List<Object> processCreatedAggrs,
			List<Object> processDeletedAggrs,
			List<Object[]> processUpdatedAggrs, String processDesc,
			boolean dontPublishWhenResultIsNull, long processFinishTime) {
		if (dontPublishWhenResultIsNull && processResult == null) {
			return;
		}
		Message msg = new Message();
		msg.setProcessDesc(processDesc);
		msg.setProcessInput(processArguments);
		msg.setProcessOutput(processResult);
		msg.setProcessCreatedAggrs(processCreatedAggrs);
		msg.setProcessDeletedAggrs(processDeletedAggrs);
		msg.setProcessUpdatedAggrs(processUpdatedAggrs);
		msg.setProcessFinishTime(processFinishTime);
		try {
			messageSender.send(msg);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void defineProcessesToPublish(List<String> processesToPublish) {
		messageSender.defineProcessesToSend(processesToPublish);
	}

}
