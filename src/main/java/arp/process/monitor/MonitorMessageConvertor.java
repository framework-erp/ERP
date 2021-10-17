package arp.process.monitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arp.process.publish.Message;
import arp.process.publish.MessageReceiver;
import arp.process.publish.MessageSender;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

public class MonitorMessageConvertor {

	private MessageReceiver<Message> messageReceiver;
	private MessageSender<MonitorMessage> messageSender;

	public MonitorMessageConvertor(MessageReceiver<Message> messageReceiver,
			MessageSender<MonitorMessage> messageSender) {
		this.messageReceiver = messageReceiver;
		this.messageSender = messageSender;
	}

	public void start(List<String> processesToPublish) {
		messageSender.defineProcessesToSend(processesToPublish);
		for (String process : processesToPublish) {
			messageReceiver.subscribeProcess(process);
		}
		new Thread(() -> {
			while (true) {
				List<Message> msgList = null;
				try {
					msgList = messageReceiver.receive();
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
					messageSender.send(msg.getProcessDesc(), monitorMessage);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}	).start();
	}

	private MonitorMessage convertMessage(Message msg) {
		MonitorMessage monitorMessage = new MonitorMessage();
		monitorMessage.setProcessDesc(msg.getProcessDesc());
		monitorMessage.setProcessInputs(JSON.toJSONString(
				msg.getProcessInput(), SerializerFeature.IgnoreNonFieldGetter));
		monitorMessage
				.setProcessOutput(JSON.toJSONString(msg.getProcessOutput(),
						SerializerFeature.IgnoreNonFieldGetter));
		List<Map> processCreatedAggrs = new ArrayList<>();
		for (Object aggr : msg.getProcessCreatedAggrs()) {
			Map aggrDto = new HashMap();
			aggrDto.put("class", aggr.getClass().getName());
			aggrDto.put("aggr", aggr);
			processCreatedAggrs.add(aggrDto);
		}
		monitorMessage.setProcessCreatedAggrs(JSON.toJSONString(
				processCreatedAggrs, SerializerFeature.IgnoreNonFieldGetter));
		List<Map> processDeletedAggrs = new ArrayList<>();
		for (Object aggr : msg.getProcessDeletedAggrs()) {
			Map aggrDto = new HashMap();
			aggrDto.put("class", aggr.getClass().getName());
			aggrDto.put("aggr", aggr);
			processDeletedAggrs.add(aggrDto);
		}
		monitorMessage.setProcessDeletedAggrs(JSON.toJSONString(
				processDeletedAggrs, SerializerFeature.IgnoreNonFieldGetter));
		List<Map[]> processUpdatedAggrs = new ArrayList<>();
		for (Object[] aggrs : msg.getProcessUpdatedAggrs()) {
			Map[] aggrDtos = new Map[2];
			Map aggrDto0 = new HashMap();
			aggrDto0.put("class", aggrs[0].getClass().getName());
			aggrDto0.put("aggr", aggrs[0]);
			aggrDtos[0] = aggrDto0;
			Map aggrDto1 = new HashMap();
			aggrDto1.put("class", aggrs[1].getClass().getName());
			aggrDto1.put("aggr", aggrs[1]);
			aggrDtos[1] = aggrDto1;
			processUpdatedAggrs.add(aggrDtos);
		}
		monitorMessage.setProcessUpdatedAggrs(JSON.toJSONString(
				processUpdatedAggrs, SerializerFeature.IgnoreNonFieldGetter));
		monitorMessage.setContextParametersTrace(msg
				.getContextParametersTrace());
		monitorMessage.setProcessFinishTime(msg.getProcessFinishTime());
		return monitorMessage;
	}

}
