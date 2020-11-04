package arp.core;

public class ProcessPublisher {

	public static void publish(String clsName, String mthName, String mthDesc) {
	}

	public static void publish(byte mthRtn, String clsName, String mthName, String mthDesc) {
	}

	public static void publish(short mthRtn, String clsName, String mthName, String mthDesc) {
	}

	public static void publish(char mthRtn, String clsName, String mthName, String mthDesc) {
	}

	public static void publish(int mthRtn, String clsName, String mthName, String mthDesc) {
	}

	public static void publish(float mthRtn, String clsName, String mthName, String mthDesc) {
	}

	public static void publish(double mthRtn, String clsName, String mthName, String mthDesc) {
	}

	public static void publish(long mthRtn, String clsName, String mthName, String mthDesc) {
	}

	public static void publish(boolean mthRtn, String clsName, String mthName, String mthDesc) {
	}

	public static void publish(Object mthRtn, String clsName, String mthName, String mthDesc) {
		String processDesc = clsName + "." + mthName + mthDesc;
		System.out.println(processDesc);
	}

}
