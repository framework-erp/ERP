package arp.core;

import java.util.Map;

public class MessageProcessor {

	private Map<String, Object> processObjects;

	public void process(Message msg) {
		String processDesc = msg.getProcessDesc();
		// TODO 字节码生成开始
		if (processDesc.equals("aaa")) {
			// TODO 注意，对于"aaa"在一个应用内可能有多个listener,要挨个执行
			return;
		}
		if (processDesc.equals("bbb")) {
			return;
		}
		// 字节码生成结束
	}

}
