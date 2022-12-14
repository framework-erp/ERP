package arp.process.publish;

import arp.process.Process;

import java.util.List;
import java.util.Map;

public class ProcessPublisher {

	public static ProcessMessageSender messageSender;

	public static void publish(Process process,
							   boolean dontPublishWhenResultIsNull,
							   long processFinishTime) {
		if (messageSender == null) {
			return;
		}
		if (dontPublishWhenResultIsNull && process.getResult() == null) {
			return;
		}
		Message msg = new Message();
		msg.setProcessDesc(process.getName());
		msg.setProcessInput(process.getArguments());
		msg.setProcessOutput(process.getResult());
		msg.setProcessCreatedAggrs(process.getCreatedAggrs());
		msg.setProcessDeletedAggrs(process.getDeletedAggrs());
		msg.setProcessUpdatedAggrs(process.getUpdatedAggrs());
		msg.setProcessFinishTime(processFinishTime);
		try {
			messageSender.send(msg);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
