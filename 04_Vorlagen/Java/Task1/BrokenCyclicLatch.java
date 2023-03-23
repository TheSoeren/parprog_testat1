package aufgabe1;

import java.util.concurrent.CountDownLatch;

public class BrokenCyclicLatch {
	private static final int NOF_ROUNDS = 10;
	private static final int NOF_THREADS = 10;
	private static CountDownLatch[] latches;
	

	private static void multiRounds(int number) throws InterruptedException {
		for (int round = 0; round < NOF_ROUNDS; round++) {
			latches[round].countDown();
			latches[round].await();
			System.out.println("Round " + round + " thread " + number);
		}
	}

	public static void main(String[] args) {
		latches = new CountDownLatch[NOF_ROUNDS];
	
		for (int i = 0; i < NOF_ROUNDS; i++) {
			latches[i] = new CountDownLatch(NOF_THREADS);
		}

		for (int count = 0; count < NOF_THREADS; count++) {
			int number = count;
			new Thread(() -> {
				try {
					multiRounds(number);
				} catch (InterruptedException e) {
					throw new AssertionError(e);
				}
			}).start();
		}
	}
}