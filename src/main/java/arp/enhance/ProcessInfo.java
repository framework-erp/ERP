package arp.enhance;

public class ProcessInfo {
	private int id;
	private String clsName;
	private String mthName;
	private String mthDesc;
	private String processName;
	private ListenerInfo listenerInfo;
	private boolean publish;
	private boolean dontPublishWhenResultIsNull;

	public ProcessInfo() {
	}

	public ProcessInfo(String clsName, String mthName, String mthDesc,
			String processName, ListenerInfo listenerInfo, boolean publish) {
		this.clsName = clsName;
		this.mthName = mthName;
		this.mthDesc = mthDesc;
		this.processName = processName;
		this.listenerInfo = listenerInfo;
		this.publish = publish;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getClsName() {
		return clsName;
	}

	public void setClsName(String clsName) {
		this.clsName = clsName;
	}

	public String getMthName() {
		return mthName;
	}

	public void setMthName(String mthName) {
		this.mthName = mthName;
	}

	public String getMthDesc() {
		return mthDesc;
	}

	public void setMthDesc(String mthDesc) {
		this.mthDesc = mthDesc;
	}

	public String getProcessName() {
		return processName;
	}

	public void setProcessName(String processName) {
		this.processName = processName;
	}

	public ListenerInfo getListenerInfo() {
		return listenerInfo;
	}

	public void setListenerInfo(ListenerInfo listenerInfo) {
		this.listenerInfo = listenerInfo;
	}

	public boolean isPublish() {
		return publish;
	}

	public void setPublish(boolean publish) {
		this.publish = publish;
	}

	public boolean isDontPublishWhenResultIsNull() {
		return dontPublishWhenResultIsNull;
	}

	public void setDontPublishWhenResultIsNull(
			boolean dontPublishWhenResultIsNull) {
		this.dontPublishWhenResultIsNull = dontPublishWhenResultIsNull;
	}

}
