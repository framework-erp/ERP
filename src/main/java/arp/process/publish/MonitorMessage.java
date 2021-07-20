package arp.process.publish;

public class MonitorMessage {

	private String processDesc;
	private String processInputs;
	private String processOutput;
	private String processCreatedAggrs;
	private String processDeletedAggrs;
	private String processUpdatedAggrs;
	private long processFinishTime;

	public String getProcessDesc() {
		return processDesc;
	}

	public void setProcessDesc(String processDesc) {
		this.processDesc = processDesc;
	}

	public String getProcessInputs() {
		return processInputs;
	}

	public void setProcessInputs(String processInputs) {
		this.processInputs = processInputs;
	}

	public String getProcessOutput() {
		return processOutput;
	}

	public void setProcessOutput(String processOutput) {
		this.processOutput = processOutput;
	}

	public String getProcessCreatedAggrs() {
		return processCreatedAggrs;
	}

	public void setProcessCreatedAggrs(String processCreatedAggrs) {
		this.processCreatedAggrs = processCreatedAggrs;
	}

	public String getProcessDeletedAggrs() {
		return processDeletedAggrs;
	}

	public void setProcessDeletedAggrs(String processDeletedAggrs) {
		this.processDeletedAggrs = processDeletedAggrs;
	}

	public String getProcessUpdatedAggrs() {
		return processUpdatedAggrs;
	}

	public void setProcessUpdatedAggrs(String processUpdatedAggrs) {
		this.processUpdatedAggrs = processUpdatedAggrs;
	}

	public long getProcessFinishTime() {
		return processFinishTime;
	}

	public void setProcessFinishTime(long processFinishTime) {
		this.processFinishTime = processFinishTime;
	}

}