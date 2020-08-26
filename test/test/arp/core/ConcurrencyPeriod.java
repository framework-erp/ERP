package test.arp.core;

import java.util.ArrayList;
import java.util.List;

public class ConcurrencyPeriod {

	private ThreadBoundaryTime startTime;
	private ThreadBoundaryTime endTime;
	private int concurrency;
	private List<ProcessPeriod> processPeriodList = new ArrayList<>();

	public ConcurrencyPeriod(ThreadBoundaryTime startTime, ThreadBoundaryTime endTime, int concurrency) {
		this.startTime = startTime;
		this.endTime = endTime;
		this.concurrency = concurrency;
	}

	public long getPeriod() {
		return endTime.getTime() - startTime.getTime();
	}

	public int countProcesses() {
		return processPeriodList.size();
	}

	public long getThroughput() {
		return (countProcesses() * 1000000000l) / getPeriod();
	}

	public boolean accept(ProcessPeriod processPeriod) {
		if (startTime.getFlg() == 0) {
			if (processPeriod.getStartTime() >= startTime.getTime()) {
				if (endTime.getFlg() == 1) {
					if (processPeriod.getStartTime() <= endTime.getTime()) {
						processPeriodList.add(processPeriod);
						return true;
					}
				} else {
					if (processPeriod.getStartTime() < endTime.getTime()) {
						processPeriodList.add(processPeriod);
						return true;
					}
				}
			}
		} else {
			if (processPeriod.getStartTime() > startTime.getTime()) {
				if (endTime.getFlg() == 1) {
					if (processPeriod.getStartTime() <= endTime.getTime()) {
						processPeriodList.add(processPeriod);
						return true;
					}
				} else {
					if (processPeriod.getStartTime() < endTime.getTime()) {
						processPeriodList.add(processPeriod);
						return true;
					}
				}
			}
		}
		return false;
	}

	public ThreadBoundaryTime getStartTime() {
		return startTime;
	}

	public void setStartTime(ThreadBoundaryTime startTime) {
		this.startTime = startTime;
	}

	public ThreadBoundaryTime getEndTime() {
		return endTime;
	}

	public void setEndTime(ThreadBoundaryTime endTime) {
		this.endTime = endTime;
	}

	public int getConcurrency() {
		return concurrency;
	}

	public void setConcurrency(int concurrency) {
		this.concurrency = concurrency;
	}

}
