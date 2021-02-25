package test.arp.core;

public class ProcessPeriod {

	private long tid;
	private long startTime;
	private long endTime;

	public ProcessPeriod(long tid, long startTime, long endTime) {
		this.tid = tid;
		this.startTime = startTime;
		this.endTime = endTime;
	}

	public long getPeriod() {
		return endTime - startTime;
	}

	public long getTid() {
		return tid;
	}

	public void setTid(long tid) {
		this.tid = tid;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

}
