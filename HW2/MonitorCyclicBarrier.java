/*
 * EID's of group members
 * eyl283
 * rch2777
 */

public class MonitorCyclicBarrier {
	int parties = 0;
	int arrival_index;
	public MonitorCyclicBarrier(int parties) {
		this.parties = parties;
		this.arrival_index = parties;
	}
	
	public int await() throws InterruptedException {
		int index;
		synchronized (this){
			arrival_index--;
			index = arrival_index;
			if(index != 0){
				wait();
			}else{
				arrival_index = parties;
				notifyAll();
			}

		}
	    return index;
	}
}
