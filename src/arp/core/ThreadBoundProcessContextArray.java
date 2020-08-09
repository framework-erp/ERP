package arp.core;

public class ThreadBoundProcessContextArray {
	private static ProcessContext[] threadProcessContextArray = new ProcessContext[1024
			+ (Unsafe.objArray_BUFFER_PAD * 2)];// 头尾要处理伪共享

	public static ProcessContext getProcessContext() {
		int iTid = (int) Thread.currentThread().getId();
		ProcessContext ctx = threadProcessContextArray[iTid + Unsafe.objArray_BUFFER_PAD];
		if (ctx == null) {
			ctx = new ProcessContext();
			threadProcessContextArray[iTid + Unsafe.objArray_BUFFER_PAD] = ctx;
		}
		return ctx;
	}
}
