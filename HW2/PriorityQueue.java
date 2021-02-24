import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
// EID 1
// EID 2

public class PriorityQueue {
	AtomicInteger size;
	int maxSize;
	Node head;

	public PriorityQueue(int maxSize) {
		// Creates a Priority queue with maximum allowed size as
		this.maxSize = maxSize;
		this.size = new AtomicInteger(0);
//		this.head = new Node(null, 100);
	}

	public int add(String name, int priority) {
		// Adds the name with its priority to this queue.
		// Returns the current position in the list where the name was inserted;
		// otherwise, returns -1 if the name is already present in the list.
		// This method blocks when the list is full.
		Node currentNode = head;
		int position = 0;
		Node newNode = new Node(name, priority);
		/////// Check is full

		if (size.get() == 0){
			head = newNode;
		}else if (head.priority > priority) {
			newNode.next = head;
			head = newNode;
		}else {
			while (currentNode.next != null & currentNode.next.priority < priority) {
				position++;
				if(currentNode.name.equals(name)) {
					return -1;
				}
				currentNode = currentNode.next;
			}
			position++;
			newNode.next = currentNode.next;
			currentNode.next = newNode;
		}
		return position;
	}

	public int search(String name) {
		// Returns the position of the name in the list;
		// otherwise, returns -1 if the name is not found.
		Node currentNode = head;
		int position = 0;
		while (currentNode.next != null && !currentNode.name.equals(name)) {
			position++;
			currentNode = currentNode.next;
		}
		if(currentNode.next != null){
			return -1;
		}else {
			return position;
		}
	}

	public String getFirst() {
		// Retrieves and removes the name with the highest priority in the list,
		// or blocks the thread if the list is empty.
		String pop = head.name;
		head = head.next;
		return pop;
	}

	public static void main(String[] args) {

	}
}
class Node {
	String name;
	int priority;
	Node next;

	Node(String name, int priority){
		this.name = name;
		this.priority = priority;
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
