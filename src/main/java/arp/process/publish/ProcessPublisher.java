package arp.process.publish;

import java.util.List;
import java.util.Map;

public class ProcessPublisher {

	public static MessageSender messageSender;

	public static void publish(List<Object> processArguments,
			Object processResult, List<Object> processCreatedAggrs,
			List<Object> processDeletedAggrs,
			List<Object[]> processUpdatedAggrs, String processDesc,
			boolean dontPublishWhenResultIsNull,
			Map<String, Object> contextParameters, long processFinishTime) {
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
		msg.setContextParameters(contextParameters);
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
