import java.util.Random;

public class testPriorityQueue implements Runnable {
    final static int Q_SIZE = 10;
    final static int T_SIZE = 5;

    final PriorityQueue queue;

    public testPriorityQueue(PriorityQueue queue) {
        this.queue = queue;
    }

    public void run() {
        for(int i=0; i < 100; i++) {
            Random rand = new Random();
            int rand_int = rand.nextInt(1000);
            if (rand_int < 950) {
                int pri = rand.nextInt(10);
                String name = "sam";
                int res = queue.add(name, pri);
//                System.out.println("Added " + name + " position " + res);
            }
            else {
                String pop =  queue.getFirst();
                System.out.println("Popped " + pop);
            }
        }
        queue.printQueue();

    }

    public static void main(String[] args) {
        PriorityQueue queue = new PriorityQueue(Q_SIZE);
        Thread[] t = new Thread[T_SIZE];

        for (int i = 0; i < T_SIZE; ++i) {
            t[i] = new Thread(new testPriorityQueue(queue));
        }

        for (int i = 0; i < T_SIZE; ++i) {
            t[i].start();
        }
    }
}