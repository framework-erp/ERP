package test.arp.core;

import test.arp.core.pack1.TestService;

/**
 * @author zheng chengdong
 */
public class TakeAndWaitWorker implements Runnable{
    private TestService service;

    public TakeAndWaitWorker(TestService service) {
        this.service = service;
    }

    @Override
    public void run() {
        TestEntity entity2 = service.takeAndWait(1,60000L);
    }
}
