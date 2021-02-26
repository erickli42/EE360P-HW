// eyl283
// rch2777
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class PriorityQueue {
	AtomicInteger size;
	final int maxSize;
	final ReentrantLock monitorLock = new ReentrantLock();
	final Condition notFull = monitorLock.newCondition();
	final Condition notEmpty = monitorLock.newCondition();
	Node head;

	public PriorityQueue(int maxSize) {
		// Creates a Priority queue with maximum allowed size as
		this.maxSize = maxSize;
		this.size = new AtomicInteger(0);
		this.head = new Node(null, 999);
	}

	public int add(String name, int priority) {
		// Adds the name with its priority to this queue.
		// Returns the current position in the list where the name was inserted;
		// otherwise, returns -1 if the name is already present in the list.
		// This method blocks when the list is full.

		Node newNode = new Node(name, priority);

		while (true) {

			// Check is full
			monitorLock.lock();
			try {
				while (size.get() >= maxSize) {
//					System.out.println(name + " is waiting with size" + size.get());
					notFull.await();
				}
			} catch(Exception e) {
				e.printStackTrace();
			} finally {
				monitorLock.unlock();
			}

			int position = -1;
			Node currentNode = head;
			while (currentNode.next != null && currentNode.next.priority >= priority) {
				position++;
				currentNode = currentNode.next;
			}
			position++;

			Node pred = currentNode;
			pred.lock.lock();
			Node succ = pred.next;
			if (pred.isDeleted.get()) {
				pred.lock.unlock();
				continue;
			}

			if (succ == null) {
				if (search(name) != -1) {
					pred.lock.unlock();
					return -1;
				}
				if (size.incrementAndGet() > maxSize) {
					pred.lock.unlock();
					size.getAndDecrement();
					continue;
				}
				// Adding to last node in the list
				pred.next = newNode;
			} else {
				succ.lock.lock();
				if (succ.isDeleted.get() || priority > pred.priority || priority < succ.priority) {
					succ.lock.unlock();
					pred.lock.unlock();
					continue;
				}
				if (search(name) != -1) {
					succ.lock.unlock();
					pred.lock.unlock();
					return -1;
				}
				if (size.incrementAndGet() > maxSize) {
					succ.lock.unlock();
					pred.lock.unlock();
					size.getAndDecrement();
					continue;
				}
				newNode.next = succ;
				pred.next = newNode;
				succ.lock.unlock();
			}
			pred.lock.unlock();
//			System.out.println("Added " + name);
			monitorLock.lock();
			notEmpty.signal();
			monitorLock.unlock();
			return position;
		}
	}

	public int search(String name) {
		// Returns the position of the name in the list;
		// otherwise, returns -1 if the name is not found.
		Node currentNode = head.next;
		int position = 0;
		while (currentNode != null) {
			if (currentNode.name.equals(name)) {
				if (currentNode.isDeleted.get()) {
					return -1;
				} else {
					return position;
				}
			}
			position++;
			currentNode = currentNode.next;
		}
		return -1;
	}

	public String getFirst() {
		// Retrieves and removes the name with the highest priority in the list,
		// or blocks the thread if the list is empty.
		while (true) {
			monitorLock.lock();
			try {
				while (size.get() <= 0) {
//					System.out.println("Pop is waiting with size" + size.get());
					notEmpty.await();
				}
			} catch(Exception e) {
				e.printStackTrace();
			} finally {
				monitorLock.unlock();
			}


			head.lock.lock();
			Node curr = head.next;
			if (head.next == null) {
				head.lock.unlock();
				continue;
			}
			String pop = curr.name;

			curr.lock.lock();

			if (curr.isDeleted.get() || head.next != curr) {
				curr.lock.unlock();
				head.lock.unlock();
				continue;
			}

			size.getAndDecrement();
			curr.isDeleted.set(true);
			head.next = curr.next;
			curr.lock.unlock();
			head.lock.unlock();

//			System.out.println("Pop " + pop);
			monitorLock.lock();
			notFull.signal();
			monitorLock.unlock();
			return pop;
		}
	}

	public synchronized void printQueue() {
		Node currentNode = head.next;
		while (currentNode != null) {
			System.out.print(currentNode.name + " ");
			currentNode = currentNode.next;
		}
		System.out.println("");
	}
}

class Node {
	String name;
	int priority;
	AtomicBoolean isDeleted;
	ReentrantLock lock;
	Node next;

	Node(String name, int priority){
		this.name = name;
		this.priority = priority;
		this.isDeleted = new AtomicBoolean(false);
		this.lock = new ReentrantLock();
		this.next = null;
	}
}
