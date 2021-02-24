package test.arp.core;

public class ThreadBoundaryTime implements Comparable<ThreadBoundaryTime> {

	private long time;

	/**
	 * 0该线程第一个过程开始时间，1该线程最后一个过程开始时间
	 */
	private int flg;

	public ThreadBoundaryTime(long time, int flg) {
		this.time = time;
		this.flg = flg;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public int getFlg() {
		return flg;
	}

	public void setFlg(int flg) {
		this.flg = flg;
	}

	@Override
	public int compareTo(ThreadBoundaryTime o) {
		return (int) (time - o.time);
	}

}
