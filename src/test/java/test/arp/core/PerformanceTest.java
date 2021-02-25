package test.arp.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import arp.enhance.ClassEnhancer;

public class PerformanceTest {

	public static void main(String[] args) {

		try {
			ClassEnhancer.parseAndEnhance("test.arp.core");
		} catch (Exception e) {
			e.printStackTrace();
		}

		TestService service = new TestService();

		int accountsCount = 100000;
		if (args.length > 0) {
			accountsCount = Integer.parseInt(args[1]);
		}
		int accountInitBalance = 100;
		for (int i = 0; i < accountsCount; i++) {
			service.f3(i, accountInitBalance);
		}

		int threadCount = 4;
		if (args.length > 0) {
			threadCount = Integer.parseInt(args[0]);
		}
		int transferCount = 1000000;
		int[][] transferAccountIdsArray = new int[transferCount][2];
		int[] transferAmountArray = new int[transferCount];
		Random r = new Random();
		for (int i = 0; i < transferCount; i++) {
			int a1 = r.nextInt(accountsCount);
			int a2 = r.nextInt(accountsCount);
			while (a2 == a1) {
				a2 = r.nextInt(accountsCount);
			}
			int am = r.nextInt(accountInitBalance);
			transferAccountIdsArray[i] = new int[] { a1, a2 };
			transferAmountArray[i] = am;
		}
		doTransfer(service, threadCount, transferCount, transferAccountIdsArray, transferAmountArray);
	}

	private static void doTransfer(TestService service, int threadCount, int transferCount,
			int[][] transferAccountIdsArray, int[] transferAmountArray) {
//		System.out.println("total:" + bank.getTotalBalance());
		long startTime = System.currentTimeMillis() + 1000;
		int transferCountForThread = transferCount / threadCount;
		Thread[] threadArray = new Thread[threadCount];
		long[][] threadTimeArray = new long[1024][];
		for (int i = 0; i < threadCount; i++) {
			int[][] accountIdsArray = new int[transferCountForThread][2];
			System.arraycopy(transferAccountIdsArray, i * transferCountForThread, accountIdsArray, 0,
					transferCountForThread);
			int[] amountArray = new int[transferCountForThread];
			System.arraycopy(transferAmountArray, i * transferCountForThread, amountArray, 0, transferCountForThread);
			threadArray[i] = new Thread(new Runnable() {
				@Override
				public void run() {
					long[] timeArray = new long[transferCountForThread * 2];
					int timeArrayIdx = 0;

					// 热身
					service.f4(accountIdsArray[0][0], accountIdsArray[0][1], amountArray[0]);

					while (System.currentTimeMillis() < startTime) {
					}

					long t1 = System.nanoTime();
					for (int i = 0; i < transferCountForThread; i++) {
						int[] accountIds = accountIdsArray[i];
						int amount = amountArray[i];
						timeArray[timeArrayIdx++] = System.nanoTime();
						service.f4(accountIds[0], accountIds[1], amount);
						timeArray[timeArrayIdx++] = System.nanoTime();
					}
					long dt = System.nanoTime() - t1;
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					System.out.println(dt);
					threadTimeArray[(int) Thread.currentThread().getId()] = timeArray;
				}
			});
		}

		long t1 = System.currentTimeMillis();
		for (int i = 0; i < threadCount; i++) {
			threadArray[i].start();
		}
		long t2 = System.currentTimeMillis();
		System.out.println("线程全部启动:" + (t2 - t1));

		try {
			Thread.sleep(6000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
//		System.out.println("total:" + bank.getTotalBalance());

		List<ThreadBoundaryTime> threadBoundaryTimeList = new ArrayList<>();
		for (int i = 0; i < threadTimeArray.length; i++) {
			long[] timeArray = threadTimeArray[i];
			if (timeArray != null) {
				threadBoundaryTimeList.add(new ThreadBoundaryTime(timeArray[0], 0));
				threadBoundaryTimeList.add(new ThreadBoundaryTime(timeArray[timeArray.length - 2], 1));
				System.out.println(
						"t" + i + "第一个过程开始于 " + timeArray[0] + " ,最后一个过程开始于 " + timeArray[timeArray.length - 2]);
			}
		}
		Collections.sort(threadBoundaryTimeList);
		ThreadBoundaryTime lastThreadBoundaryTime = null;
		int threadBoundaryTimeListIdx = 0;
		int concurrency = 0;
		List<ConcurrencyPeriod> concurrencyPeriodList = new ArrayList<>();
		while (true) {
			ThreadBoundaryTime threadBoundaryTime = threadBoundaryTimeList.get(threadBoundaryTimeListIdx);
			if (threadBoundaryTimeListIdx == 0) {
				lastThreadBoundaryTime = threadBoundaryTime;
			} else {
				// 如果是最后一个，那就必须要处理
				if (threadBoundaryTimeListIdx == (threadBoundaryTimeList.size() - 1)) {
					concurrencyPeriodList
							.add(new ConcurrencyPeriod(lastThreadBoundaryTime, threadBoundaryTime, concurrency));
					break;
				} else {
					if (threadBoundaryTime.getTime() != lastThreadBoundaryTime.getTime()) {
						concurrencyPeriodList
								.add(new ConcurrencyPeriod(lastThreadBoundaryTime, threadBoundaryTime, concurrency));
					}
					lastThreadBoundaryTime = threadBoundaryTime;
				}
			}
			if (threadBoundaryTime.getFlg() == 0) {
				concurrency++;
			} else {
				concurrency--;
			}
			threadBoundaryTimeListIdx++;
		}
		for (int i = 0; i < threadTimeArray.length; i++) {
			long[] timeArray = threadTimeArray[i];
			if (timeArray != null) {
				for (int j = 0; j < (timeArray.length / 2); j++) {
					ProcessPeriod processPeriod = new ProcessPeriod(i, timeArray[j * 2], timeArray[j * 2 + 1]);
					for (ConcurrencyPeriod concurrencyPeriod : concurrencyPeriodList) {
						if (concurrencyPeriod.accept(processPeriod)) {
							break;
						}
					}
				}
			}
		}

		System.out.println();
		concurrencyPeriodList.forEach((concurrencyPeriod) -> {
			System.out.println(concurrencyPeriod.getConcurrency() + "并发时段, 持续:" + concurrencyPeriod.getPeriod()
					+ ", 完成过程:" + concurrencyPeriod.countProcesses() + ", 吞吐量:" + concurrencyPeriod.getThroughput());
		});
		System.out.println("done");

	}

}
