package arp.process.publish;

public class Message {

	private String processDesc;
	// TODO processInput
	private Object processOutput;

	// TODO aggrs changed

	public String getProcessDesc() {
		return processDesc;
	}

	public void setProcessDesc(String processDesc) {
		this.processDesc = processDesc;
	}

	public Object getProcessOutput() {
		return processOutput;
	}

	public void setProcessOutput(Object processOutput) {
		this.processOutput = processOutput;
	}

}
