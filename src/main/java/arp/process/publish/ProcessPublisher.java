package arp.process.publish;

public class ProcessPublisher {

	public static MessageSender messageSender;

	public static void publish(String clsName, String mthName, String processName,
			boolean dontPublishWhenResultIsNull) {
		doPublish(null, clsName, mthName, processName, dontPublishWhenResultIsNull);
	}

	public static void publish(byte mthRtn, String clsName, String mthName, String processName,
			boolean dontPublishWhenResultIsNull) {
		doPublish(mthRtn, clsName, mthName, processName, dontPublishWhenResultIsNull);
	}

	public static void publish(short mthRtn, String clsName, String mthName, String processName,
			boolean dontPublishWhenResultIsNull) {
		doPublish(mthRtn, clsName, mthName, processName, dontPublishWhenResultIsNull);
	}

	public static void publish(char mthRtn, String clsName, String mthName, String processName,
			boolean dontPublishWhenResultIsNull) {
		doPublish(mthRtn, clsName, mthName, processName, dontPublishWhenResultIsNull);
	}

	public static void publish(int mthRtn, String clsName, String mthName, String processName,
			boolean dontPublishWhenResultIsNull) {
		doPublish(mthRtn, clsName, mthName, processName, dontPublishWhenResultIsNull);
	}

	public static void publish(float mthRtn, String clsName, String mthName, String processName,
			boolean dontPublishWhenResultIsNull) {
		doPublish(mthRtn, clsName, mthName, processName, dontPublishWhenResultIsNull);
	}

	public static void publish(double mthRtn, String clsName, String mthName, String processName,
			boolean dontPublishWhenResultIsNull) {
		doPublish(mthRtn, clsName, mthName, processName, dontPublishWhenResultIsNull);
	}

	public static void publish(long mthRtn, String clsName, String mthName, String processName,
			boolean dontPublishWhenResultIsNull) {
		doPublish(mthRtn, clsName, mthName, processName, dontPublishWhenResultIsNull);
	}

	public static void publish(boolean mthRtn, String clsName, String mthName, String processName,
			boolean dontPublishWhenResultIsNull) {
		doPublish(mthRtn, clsName, mthName, processName, dontPublishWhenResultIsNull);
	}

	public static void publish(Object mthRtn, String clsName, String mthName, String processName,
			boolean dontPublishWhenResultIsNull) {
		doPublish(mthRtn, clsName, mthName, processName, dontPublishWhenResultIsNull);
	}

	private static void doPublish(Object mthRtn, String clsName, String mthName, String processName,
			boolean dontPublishWhenResultIsNull) {
		if (dontPublishWhenResultIsNull && mthRtn == null) {
			return;
		}
		String processDesc;
		if (!processName.trim().isEmpty()) {
			processDesc = processName;
		} else {
			processDesc = clsName + "." + mthName;
		}
		Message msg = new Message();
		msg.setProcessDesc(processDesc);
		msg.setProcessOutput(mthRtn);
		try {
			messageSender.send(msg);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
