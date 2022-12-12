package arp.process.publish;

import java.util.List;
import java.util.Map;

public class Message {

	private String processDesc;
	private List<Object> processInput;
	private Object processOutput;
	private List<Object> processCreatedAggrs;
	private List<Object> processDeletedAggrs;
	private List<Object[]> processUpdatedAggrs;
	private long processFinishTime;

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

	public List<Object> getProcessCreatedAggrs() {
		return processCreatedAggrs;
	}

	public void setProcessCreatedAggrs(List<Object> processCreatedAggrs) {
		this.processCreatedAggrs = processCreatedAggrs;
	}

	public List<Object> getProcessDeletedAggrs() {
		return processDeletedAggrs;
	}

	public void setProcessDeletedAggrs(List<Object> processDeletedAggrs) {
		this.processDeletedAggrs = processDeletedAggrs;
	}

	public List<Object[]> getProcessUpdatedAggrs() {
		return processUpdatedAggrs;
	}

	public void setProcessUpdatedAggrs(List<Object[]> processUpdatedAggrs) {
		this.processUpdatedAggrs = processUpdatedAggrs;
	}

	public long getProcessFinishTime() {
		return processFinishTime;
	}

	public void setProcessFinishTime(long processFinishTime) {
		this.processFinishTime = processFinishTime;
	}

}
