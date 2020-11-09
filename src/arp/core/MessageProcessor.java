package arp.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageProcessor {

	// listener处理对象
	private static Object[] listenerProcessObjects = new Object[2];

	// 过程的listeners索引
	private static Map<String, List<Integer>> processListenersIndex = new HashMap<>();

	public static void defineListener(int idx, String processDesc) {
		List<Integer> list = processListenersIndex.get(processDesc);
		if (list == null) {
			list = new ArrayList<>();
			processListenersIndex.put(processDesc, list);
		}
		list.add(idx);
	}

	public static void addListener(int idx, Object listener) {
		if (idx == (listenerProcessObjects.length - 1)) {
			resizeListenerProcessObjects(idx);
		}
		listenerProcessObjects[idx] = listener;
	}

	private static void resizeListenerProcessObjects(int idx) {
		Object[] old = listenerProcessObjects;
		listenerProcessObjects = new Object[listenerProcessObjects.length * 2];
		System.arraycopy(old, 0, listenerProcessObjects, 0, idx + 1);
	}

	public void process(Message msg) {
		String processDesc = msg.getProcessDesc();
		List<Integer> listenersIndex = processListenersIndex.get(processDesc);
		if (listenersIndex == null) {
			return;
		}
		for (int listenerIndex : listenersIndex) {
			processByListener(listenerIndex, listenerProcessObjects[listenerIndex], msg);
		}
	}

	private void processByListener(int listenerIndex, Object listenerProcessObject, Message msg) {
		// TODO 字节码生成开始
		if (listenerIndex == 0) {
//			service1 = listenerProcessObject;
//			 service1.receiveAAA()
			return;
		}
		if (listenerIndex == 1) {
			// service2.receiveAAA()
			return;
		}
		if (listenerIndex == 2) {
			// service2.receiveBBB()
			return;
		}
		// 字节码生成结束
	}

}
