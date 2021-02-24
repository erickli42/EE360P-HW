public class testPriorityQueue implements Runnable {
    final static int SIZE = 5;
    final static int ROUND = 5;

    //	final CyclicBarrier gate;
    final MonitorCyclicBarrier gate;

    //	public testCyclicBarrier(CyclicBarrier gate) {
//		this.gate = gate;
//	}
    public testPriorityQueue(MonitorCyclicBarrier gate) {
        this.gate = gate;
    }

    public void run() {
        int index = -1;

        for (int round = 0; round < ROUND; ++round) {
            System.out.println("Thread " + Thread.currentThread().getId() + " is WAITING round:" + round);
            try {
                index = gate.await();
//				System.out.println("Thread " + Thread.currentThread().getId() + " is index:" + index);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Thread " + Thread.currentThread().getId() + " is leaving round:" + round);
        }
    }

    public static void main(String[] args) {
//		CyclicBarrier gate = new CyclicBarrier(SIZE);
        MonitorCyclicBarrier gate = new MonitorCyclicBarrier(SIZE);
        Thread[] t = new Thread[SIZE];

        for (int i = 0; i < SIZE; ++i) {
            t[i] = new Thread(new testCyclicBarrier(gate));
        }

        for (int i = 0; i < SIZE; ++i) {
            t[i].start();
        }
    }
}