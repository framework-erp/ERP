package arp.process.publish;

import java.util.List;

public interface MessageReceiver<T> {

	List<T> receive() throws Exception;

	void subscribeProcess(String processDesc);

	List<String> queryAllProcessesToSubscribe();

}
