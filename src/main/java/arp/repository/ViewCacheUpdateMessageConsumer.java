package arp.repository;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import arp.process.publish.Message;
import arp.process.publish.ProcessMessageReceiver;

public class ViewCacheUpdateMessageConsumer {

	private ProcessMessageReceiver receiver;
	private ExecutorService executorService;
	private Map<String, ViewCachedRepository<?, ?>> repositories = new ConcurrentHashMap<>();

	public ViewCacheUpdateMessageConsumer(ProcessMessageReceiver receiver) {
		this.receiver = receiver;
		executorService = Executors.newCachedThreadPool();
	}

	public void registerRepository(ViewCachedRepository<?, ?> repository,
			Class<?> entityType) {
		repositories.put(entityType.getName(), repository);
	}

	public void start() {
		new Thread(
				() -> {
					while (true) {
						List<Message> msgList = null;
						try {
							msgList = receiver.receive();
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						if (msgList == null) {
							continue;
						}
						for (Message msg : msgList) {
							List<Map<String, Object>> contextParametersTrace = msg
									.getContextParametersTrace();
							if (contextParametersTrace == null
									|| contextParametersTrace.isEmpty()) {
								continue;
							}
							Map<String, Object> contextParameters = contextParametersTrace
									.get(contextParametersTrace.size() - 1);
							if (contextParameters == null) {
								continue;
							}
							Map map = (Map) contextParameters
									.get("idsForEntityUpdatedWithViewCachedRepository");
							if (map == null || map.isEmpty()) {
								continue;
							}
							executorService.submit(() -> {
								for (Object obj : map.entrySet()) {
									Entry entry = (Entry) obj;
									ViewCachedRepository repo = repositories
											.get(entry.getKey());
									if (repo != null) {
										List ids = (List) entry.getValue();
										for (Object id : ids) {
											repo.updateCacheFromUnderlyingForEntity(id);
										}
									}
								}
							});
						}
					}
				}).start();
	}

}
