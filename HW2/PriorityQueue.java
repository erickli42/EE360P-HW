//import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
// EID 1
// EID 2

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

//		// Check is full
//		monitorLock.lock();
//		try {
//			size.getAndIncrement();
//			while (size.get() >= maxSize) {
//				notFull.await();
//			}
//		} catch(Exception e) {
//			e.printStackTrace();
//		} finally {
//			monitorLock.unlock();
//		}

		int position = -1;
		while (true) {
				position = -1;
				Node currentNode = head;
				while (currentNode.next != null && currentNode.next.priority >= priority) {
					position++;
					currentNode = currentNode.next;
				}
				position++;

				Node pred = currentNode;
				pred.lock.lock();
				Node succ = pred.next;

//				String succName;
//				if (succ == null) {
//					succName = "null";
//				} else {
//					succName = succ.name;
//				}
//				System.out.println("prio " + priority + " pred " + pred.name + " succ " + succName);

				if (pred.isDeleted.get()) {
					pred.lock.unlock();
					continue;
				}
				if (succ == null) {
					if (search(name) != -1) {
						pred.lock.unlock();
						return -1;
					}
					// Adding to last node in the list
					pred.next = newNode;
//					System.out.println("Added " + name + " at " + position + " is before null");
				} else {
					succ.lock.lock();
					if (succ.isDeleted.get() || pred.next != succ || priority < succ.priority || priority > pred.priority) {
						succ.lock.unlock();
						pred.lock.unlock();
						continue;
					}
					if (search(name) != -1) {
						succ.lock.unlock();
						pred.lock.unlock();
						return -1;
					}
					newNode.next = succ;
					pred.next = newNode;
					if (priority < succ.priority) {
						System.out.println("Added " + name + " at " + position + " is before " + succ.name);
					}
					succ.lock.unlock();
				}
				pred.lock.unlock();
				break;
//			}
		}
		// Signal not empty
		size.getAndIncrement();
		return position;
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

	public synchronized void printQueue() {
		// Returns the position of the name in the list;
		// otherwise, returns -1 if the name is not found.
		Node currentNode = head.next;
		while (currentNode != null) {
			System.out.print(currentNode.name + " ");
			currentNode = currentNode.next;
		}
		System.out.println("");
	}

	public String getFirst() {
		// Retrieves and removes the name with the highest priority in the list,
		// or blocks the thread if the list is empty.

//		monitorLock.lock();
//		try {
//			while (size.get() == 0) {
//				notEmpty.await();
//			}
//		} catch(Exception e) {
//			e.printStackTrace();
//		} finally {
//			monitorLock.unlock();
//		}

		String pop;

		while (true) {
			Node prev = head;
			Node curr = head.next;
			pop = curr.name;

			prev.lock.lock();
			curr.lock.lock();

			if (curr.isDeleted.get() || prev.next != curr) {
				curr.lock.unlock();
				prev.lock.unlock();
				continue;
			}

			curr.isDeleted.set(true);
			prev.next = curr.next;
			curr.lock.unlock();
			prev.lock.unlock();
			break;
		}
		size.getAndDecrement();
		return pop;
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

//	public void setNext(Node next) {
//		this.next = next;
//	}
//
//	public int getPriority() {
//		return priority;
//	}
//
//	public String getName() {
//		return name;
//	}
}
