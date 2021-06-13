package arp.process;

public class ProcessWrapper {

	public static void beforeProcessStart() {
		ProcessContext processContext = ThreadBoundProcessContextArray
				.getProcessContext();
		processContext.startProcess();
	}

	public static void afterProcessFinish() {
		ProcessContext processContext = ThreadBoundProcessContextArray
				.getProcessContext();
		processContext.finishProcess();
	}

	public static void afterProcessFaild() {
		ProcessContext processContext = ThreadBoundProcessContextArray
				.getProcessContext();
		processContext.processFaild();
	}

	public static void recordProcessDesc(String clsName, String mthName,
			String processName) {
		// TODO
	}

	public static void recordProcessResult(Object result) {
		// TODO
	}

	public static void setDontPublishWhenResultIsNull(
			boolean dontPublishWhenResultIsNull) {
		// TODO
	}

}
