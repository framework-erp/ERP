package arp.enhance;

import java.util.Map;

public class ResolvedClass {
	private String name;
	private Map<String, ProcessInfo> processInfos;
	private byte[] classBytes;

	public ResolvedClass(String name, Map<String, ProcessInfo> processInfos,
			byte[] classBytes) {
		this.name = name;
		this.processInfos = processInfos;
		this.classBytes = classBytes;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<String, ProcessInfo> getProcessInfos() {
		return processInfos;
	}

	public void setProcessInfos(Map<String, ProcessInfo> processInfos) {
		this.processInfos = processInfos;
	}

	public byte[] getClassBytes() {
		return classBytes;
	}

	public void setClassBytes(byte[] classBytes) {
		this.classBytes = classBytes;
	}

}
