package arp.process.publish;

import java.util.List;

public class Message {

	private String processDesc;
	private List<Object> processInput;
	private Object processOutput;
	private List<Object[]> processUpdatedAggrs;

	public String getProcessDesc() {
		return processDesc;
	}

	public void setProcessDesc(String processDesc) {
		this.processDesc = processDesc;
	}

	public List<Object> getProcessInput() {
		return processInput;
	}

	public void setProcessInput(List<Object> processInput) {
		this.processInput = processInput;
	}

	public Object getProcessOutput() {
		return processOutput;
	}

	public void setProcessOutput(Object processOutput) {
		this.processOutput = processOutput;
	}

	public List<Object[]> getProcessUpdatedAggrs() {
		return processUpdatedAggrs;
	}

	public void setProcessUpdatedAggrs(List<Object[]> processUpdatedAggrs) {
		this.processUpdatedAggrs = processUpdatedAggrs;
	}

}
