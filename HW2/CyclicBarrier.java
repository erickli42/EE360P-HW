/*
 * EID's of group members
 * eyl283
 * rch2777
 */
import java.util.concurrent.Semaphore; // for implementation using Semaphores

public class CyclicBarrier {
	int parties = 0;
	Semaphore barriar_sem = null;
	int arrival_index;
	Semaphore index_sem;
	Semaphore round_sem;
	public CyclicBarrier(int parties) {
		this.parties = parties;
		this.arrival_index = parties;
		this.barriar_sem = new Semaphore(0);
		this.index_sem = new Semaphore(1);
		this.round_sem = new Semaphore(parties);
//		System.out.println("Permits: " + sem.availablePermits());
	}
	
	public int await() throws InterruptedException {
		round_sem.acquire();
		index_sem.acquire();
		arrival_index--;
		int index = arrival_index;
		if(index == 0){
			barriar_sem.release(parties);
//			arrival_index = parties;
		}
		index_sem.release();
//		System.out.println("Permits: " + barriar_sem.availablePermits());
		barriar_sem.acquire();
//		System.out.println("Permits: " + sem.availablePermits());
//		if(index == 0){
//			round_sem.release(parties);
//		}
		index_sem.acquire();
		arrival_index++;
		if(arrival_index == parties){
			round_sem.release(parties);
		}
		index_sem.release();
	    return index;
	}
}
