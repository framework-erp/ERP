package arp.core;

import java.util.List;
import java.util.Map;

public class MessageProcessor {

	private static Object[] listenerProcessObjects;// listener处理对象
	private static int listenerProcessObjectsIdx;

	private static Map<String, List<Integer>> processListenersIndex;// 过程的listeners索引

	public static void defineListener(int idx, String processDesc) {

	}

	public static void addListener(int idx, String processDesc) {

	}

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
