package arp.enhance;

import org.objectweb.asm.Type;

public class ListenerInfo {
	private String processDesc;// 源过程描述
	private Type processOutputType;// 源过程输出类型
	private String listenerProcessObjType;// listener所在的处理类类型
	private String listenerMthName;
	private String listenerMthDesc;
	private String messageProcessorClasseType;

	public ListenerInfo() {
	}

	public ListenerInfo(String processDesc, Type processOutputType,
			String listenerProcessObjType, String listenerMthName,
			String listenerMthDesc) {
		this.processDesc = processDesc;
		this.processOutputType = processOutputType;
		this.listenerProcessObjType = listenerProcessObjType;
		this.listenerMthName = listenerMthName;
		this.listenerMthDesc = listenerMthDesc;
	}

	public String getProcessDesc() {
		return processDesc;
	}

	public void setProcessDesc(String processDesc) {
		this.processDesc = processDesc;
	}

	public Type getProcessOutputType() {
		return processOutputType;
	}

	public void setProcessOutputType(Type processOutputType) {
		this.processOutputType = processOutputType;
	}

	public String getListenerProcessObjType() {
		return listenerProcessObjType;
	}

	public void setListenerProcessObjType(String listenerProcessObjType) {
		this.listenerProcessObjType = listenerProcessObjType;
	}

	public String getListenerMthName() {
		return listenerMthName;
	}

	public void setListenerMthName(String listenerMthName) {
		this.listenerMthName = listenerMthName;
	}

	public String getListenerMthDesc() {
		return listenerMthDesc;
	}

	public void setListenerMthDesc(String listenerMthDesc) {
		this.listenerMthDesc = listenerMthDesc;
	}

	public String getMessageProcessorClasseType() {
		return messageProcessorClasseType;
	}

	public void setMessageProcessorClasseType(String messageProcessorClasseType) {
		this.messageProcessorClasseType = messageProcessorClasseType;
	}

}
