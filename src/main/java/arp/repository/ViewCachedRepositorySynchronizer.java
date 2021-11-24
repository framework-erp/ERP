package arp.repository;

import java.util.List;

import arp.enhance.ClassEnhancer;
import arp.enhance.ProcessInfo;
import arp.process.publish.ProcessMessageReceiver;

public class ViewCachedRepositorySynchronizer {

	private static ViewCacheUpdateMessageConsumer viewCacheUpdateMessageConsumer;

	public static void registerRepository(ViewCachedRepository<?, ?> repo,
			Class<?> entityType) {
		viewCacheUpdateMessageConsumer.registerRepository(repo, entityType);
	}

	public static void start(ProcessMessageReceiver receiver) {
		viewCacheUpdateMessageConsumer = new ViewCacheUpdateMessageConsumer(
				receiver);
		List<ProcessInfo> processInfoList = ClassEnhancer.parseResult
				.getProcessInfoList();
		for (ProcessInfo processInfo : processInfoList) {
			String processDesc;
			if (!processInfo.getProcessName().trim().isEmpty()) {
				processDesc = processInfo.getProcessName();
			} else {
				processDesc = processInfo.getClsName() + "."
						+ processInfo.getMthName();
			}
			viewCacheUpdateMessageConsumer.subscribeProcess(processDesc);
		}
		viewCacheUpdateMessageConsumer.start();
	}
}
