// eyl283
// rch2777
import java.util.concurrent.locks.*;
import java.util.Random;

public class FairUnifanBathroom {
    final int size = 4;
    final ReentrantLock monitorLock = new ReentrantLock(true);
    final Condition nextInLine  = monitorLock.newCondition();
    final Condition notFull = monitorLock.newCondition();
    final Condition isEmpty = monitorLock.newCondition();

    int ticketNumber=0, nextTicket=0, count=0;
    boolean utInside=false, ouInside=false;

    /**************** FOR TESTING **********************/
//    public static void main(String[] args) {
//        FairUnifanBathroom bathroom = new FairUnifanBathroom();
//
//        for (int i = 0; i < 25; i++) {
//            new Thread(() -> { // Create UT Thread
//                try {
//                    bathroom.enterBathroomUT();
//                    Thread.sleep(new Random().nextInt(500));
//                    bathroom.leaveBathroomUT();
//                } catch(Exception e) {
//                    e.printStackTrace();
//                }
//            }).start();
//
//            new Thread(() -> { // Create OU Thread
//                try {
//                    bathroom.enterBathroomOU();
//                    Thread.sleep(new Random().nextInt(500));
//                    bathroom.leaveBathroomOU();
//                } catch(Exception e) {
//                    e.printStackTrace();
//                }
//            }).start();
//        }
//    }

    public void enterBathroomUT() throws InterruptedException{
        // Called when a UT fan wants to enter bathroom
        monitorLock.lock();
        try {
            // Assign ticket number
            int myTicket = ticketNumber;
            ticketNumber++;
            //System.out.println("UT-" + myTicket);

            while (nextTicket != myTicket) {
                nextInLine.await();
            }

            if (utInside) {
                // If UT students are in bathroom, wait until the bathroom is not full
                while (count == size) {
                    notFull.await();
                }
            } else if (ouInside) {
                // If OU students are in the bathroom, wait until the bathroom is empty
                while (count > 0) {
                    isEmpty.await();
                }
            }
            // enter bathroom
            //System.out.println("UT-" + myTicket + " entered bathroom");
            if (count == 0) {
                utInside = true;
            }
            count++;
            nextTicket++;
            nextInLine.signal();
        } finally {
            monitorLock.unlock();
        }
    }

    public void enterBathroomOU() throws InterruptedException{
        // Called when a OU fan wants to enter bathroom
        monitorLock.lock();
        try {
            // Assign ticket number
            int myTicket = ticketNumber;
            ticketNumber++;
            //System.out.println("OU-" + myTicket);

            while (nextTicket != myTicket) {
                nextInLine.await();
            }

            if (ouInside) {
                // If OU students are in the bathroom, wait until the bathroom is not full
                while (count == size) {
                    notFull.await();
                }
            } else if (utInside) {
                // If UT students are in the bathroom, wait until the bathroom is empty
                while (count > 0) {
                    isEmpty.await();
                }
            }
            // enter bathroom
            if (count == 0) {
                ouInside = true;
            }
            //System.out.println("OU-" + myTicket + " entered bathroom");
            count++;
            nextTicket++;
            nextInLine.signal();
        } finally {
            monitorLock.unlock();
        }
    }

    public void leaveBathroomUT() {
        // Called when a UT fan wants to leave bathroom
        monitorLock.lock();
        try {
            //System.out.println("UT left bathroom");
            count--;
            if (count == size-1) {
                notFull.signal();
            } else if (count == 0) {
                utInside = false;
                isEmpty.signal();
            }
        } finally {
            monitorLock.unlock();
        }
    }

    public void leaveBathroomOU() {
        // Called when a OU fan wants to leave bathroom
        monitorLock.lock();
        try {
            //System.out.println("OU left bathroom");
            count--;
            if (count == size-1) {
                notFull.signal();
            } else if (count == 0) {
                ouInside = false;
                isEmpty.signal();
            }
        } finally {
            monitorLock.unlock();
        }
    }
}
