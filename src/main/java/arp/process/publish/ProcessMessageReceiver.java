package arp.process.publish;

import java.util.List;

public interface ProcessMessageReceiver {

	List<Message> receive() throws Exception;

}
