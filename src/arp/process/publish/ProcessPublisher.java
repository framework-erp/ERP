package arp.process.publish;

public class ProcessPublisher {

	public static MessageSender messageSender;

	public static void publish(String clsName, String mthName, String mthDesc) {
		doPublish(null, clsName, mthName, mthDesc);
	}

	public static void publish(byte mthRtn, String clsName, String mthName, String mthDesc) {
		doPublish(mthRtn, clsName, mthName, mthDesc);
	}

	public static void publish(short mthRtn, String clsName, String mthName, String mthDesc) {
		doPublish(mthRtn, clsName, mthName, mthDesc);
	}

	public static void publish(char mthRtn, String clsName, String mthName, String mthDesc) {
		doPublish(mthRtn, clsName, mthName, mthDesc);
	}

	public static void publish(int mthRtn, String clsName, String mthName, String mthDesc) {
		doPublish(mthRtn, clsName, mthName, mthDesc);
	}

	public static void publish(float mthRtn, String clsName, String mthName, String mthDesc) {
		doPublish(mthRtn, clsName, mthName, mthDesc);
	}

	public static void publish(double mthRtn, String clsName, String mthName, String mthDesc) {
		doPublish(mthRtn, clsName, mthName, mthDesc);
	}

	public static void publish(long mthRtn, String clsName, String mthName, String mthDesc) {
		doPublish(mthRtn, clsName, mthName, mthDesc);
	}

	public static void publish(boolean mthRtn, String clsName, String mthName, String mthDesc) {
		doPublish(mthRtn, clsName, mthName, mthDesc);
	}

	public static void publish(Object mthRtn, String clsName, String mthName, String mthDesc) {
		doPublish(mthRtn, clsName, mthName, mthDesc);
	}

	private static void doPublish(Object mthRtn, String clsName, String mthName, String mthDesc) {
		String processDesc = clsName + "." + mthName + mthDesc;
		Message msg = new Message();
		msg.setProcessDesc(processDesc);
		msg.setProcessOutput(mthRtn);
		messageSender.send(msg);
	}

}
