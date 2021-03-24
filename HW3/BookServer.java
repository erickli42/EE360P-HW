import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class BookServer {
  static AtomicInteger recordID = new AtomicInteger(1);
  static final List<InventoryEntry> inventory = Collections.synchronizedList(new ArrayList<InventoryEntry>());
  static final List<BorrowEntry> records = Collections.synchronizedList(new ArrayList<BorrowEntry>());

  public static class InventoryEntry {
    String bookName;
    int quantity;

    public InventoryEntry(String bookName, int quantity) {
      this.bookName = bookName;
      this.quantity = quantity;
    }
  }

  public static class BorrowEntry {
    int recordID;
    String studentName;
    String bookName;

    public BorrowEntry(int recordID, String studentName, String bookName) {
      this.recordID = recordID;
      this.studentName = studentName;
      this.bookName = bookName;
    }
  }

  public static void main (String[] args) {
    int tcpPort;
    int udpPort;
    if (args.length != 1) {
      System.out.println("ERROR: Provide 1 argument: input file containing initial inventory");
      System.exit(-1);
    }
    String fileName = args[0];
    tcpPort = 7000;
    udpPort = 8000;

    // parse the inventory file
    try {
      File inputFile = new File(fileName);
      Scanner sc = new Scanner(inputFile);
      while (sc.hasNextLine()) {
        String line = sc.nextLine();
        int lastIndex = line.lastIndexOf(" ");
        String book = line.substring(0, lastIndex);
        int quantity = Integer.parseInt(line.substring(lastIndex+1, line.length()));
        inventory.add(new InventoryEntry(book, quantity));
      }
      sc.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }

    // TODO: handle request from clients
    // TCP
    Runnable tcpHandler = () -> {
      try {
        ServerSocket listener = new ServerSocket(tcpPort);
        Socket s;
        while ( (s = listener.accept()) != null) {
          Thread t = new ServerThread(s);
          t.start();
        }
      } catch (IOException e) {
        System.err.println("Server aborted:" + e);
      }
    };
    new Thread(tcpHandler).start();

    // UDP
    Runnable udpHandler = () -> {
      DatagramPacket datapacket, returnpacket;
      int len = 1024;
      try {
        DatagramSocket datasocket = new DatagramSocket(udpPort);
        byte[] buf = new byte[len];
        while (true) {
          datapacket = new DatagramPacket(buf, buf.length);
          datasocket.receive(datapacket);
          String command = new String(datapacket.getData(), 0, datapacket.getLength());
          String[] tokens = command.split(" ");
          String response = "";
          System.out.println("UDP received:" + command);

          if (tokens[0].equals("setmode")) {
            String mode = tokens[1];
            if (mode.equals("U")) {
              response = "The communication mode is set to UDP";
            } else {
              response = "The communication mode is set to TCP";
            }
          } else if (tokens[0].equals("borrow")) {
            String studentName = tokens[1];
            StringBuilder bookName = new StringBuilder(tokens[2]);
            for (int i = 3; i < tokens.length; i++) {
              bookName.append(" ").append(tokens[i]);
            }
            // check inventory for book
            synchronized (inventory) {
              synchronized (records) {
                InventoryEntry libraryBook = findBook(bookName.toString().toString());
                if (libraryBook == null) {
                  response = "Request Failed - We do not have this book";
                } else if (libraryBook.quantity == 0) {
                  response = "Request Failed - Book not available";
                } else {
                  // Book available, create record and update inventory
                  int rid = recordID.getAndIncrement();
                  records.add(new BorrowEntry(rid, studentName, bookName.toString().toString()));
                  libraryBook.quantity--;
                  response = "Your request has been approved, " + rid + " " + studentName + " " + bookName;
                }
              }
            }
          } else if (tokens[0].equals("return")) {
            int rid = Integer.parseInt(tokens[1]);
            // check records
            synchronized (inventory) {
              synchronized (records) {
                BorrowEntry borrowedBook = findRecord(rid);
                if (borrowedBook == null) {
                  response = rid + " not found, no such borrow record";
                } else {
                  // borrow record found, remove record and update inventory
                  InventoryEntry libraryBook = findBook(borrowedBook.bookName);
                  libraryBook.quantity++;
                  records.remove(borrowedBook);
                  response = rid + " is returned";
                }
              }
            }
          } else if (tokens[0].equals("list")) {
            String studentName = tokens[1];
            // search for all books that student borrowed
            List<String> studentsBorrows = new ArrayList<String>();
            boolean studentFound = false;
            for (BorrowEntry be : records) {
              if (be.studentName.equals(studentName)) {
                studentFound = true;
                studentsBorrows.add(be.recordID + " " + be.bookName);
              }
            }
            if (!studentFound) {
              response = "No record found for " + studentName;
            } else {
              StringBuilder buildResponse = new StringBuilder(studentsBorrows.size() + "\n");
              for (String s : studentsBorrows) {
                buildResponse.append(s).append("\n");
              }
              response = buildResponse.toString();
            }
          } else if (tokens[0].equals("inventory")) {
            StringBuilder buildResponse = new StringBuilder(inventory.size() + "\n");
            for (InventoryEntry ie : inventory) {
              buildResponse.append(ie.bookName).append(" ").append(ie.quantity).append("\n");
            }
            response = buildResponse.toString();
          } else if (tokens[0].equals("exit")) {
            // close connection and print inventory to inventory.txt
            StringBuilder buildResponse = new StringBuilder(inventory.size() + "\n");
            for (InventoryEntry ie : inventory) {
              buildResponse.append(ie.bookName).append(" ").append(ie.quantity).append("\n");
            }
            response = buildResponse.toString();
          }
          byte[] buffer = response.getBytes();
          returnpacket = new DatagramPacket(
                  buffer,
                  buffer.length,
                  datapacket.getAddress(),
                  datapacket.getPort());
          datasocket.send(returnpacket);
        }
      } catch (IOException e) {
        System.err.println(e);
      }
    };
    new Thread(udpHandler).start();
  }


  public static class ServerThread extends Thread {
    Socket theClient;

    public ServerThread(Socket s) {
      theClient = s;
    }

    public void run() {
      try {
        Scanner sc = new Scanner(theClient.getInputStream());
        PrintWriter pout = new PrintWriter(theClient.getOutputStream());
        while (sc.hasNextLine()) {
          String command = sc.nextLine();
          String[] tokens = command.split(" ");
          System.out.println("TCP received:" + command);
          if (tokens[0].equals("setmode")) {
            String mode = tokens[1];
            if (mode.equals("U")) {
              pout.println("The communication mode is set to UDP");
            } else {
              pout.println("The communication mode is set to TCP");
            }
          } else if (tokens[0].equals("borrow")) {
            String studentName = tokens[1];
            StringBuilder bookName = new StringBuilder(tokens[2]);
            for (int i = 3; i < tokens.length; i++) {
              bookName.append(" ").append(tokens[i]);
            }
            // check inventory for book
            synchronized (inventory) {
              synchronized (records) {
                InventoryEntry libraryBook = findBook(bookName.toString().toString());
                if (libraryBook == null) {
                  pout.println("Request Failed - We do not have this book");
                } else if (libraryBook.quantity == 0) {
                  pout.println("Request Failed - Book not available");
                } else {
                  // Book available, create record and update inventory
                  int rid = recordID.getAndIncrement();
                  records.add(new BorrowEntry(rid, studentName, bookName.toString().toString()));
                  libraryBook.quantity--;
                  pout.println("Your request has been approved, " + rid + " " + studentName + " " + bookName);
                }
              }
            }
          } else if (tokens[0].equals("return")) {
            int rid = Integer.parseInt(tokens[1]);
            // check records
            synchronized (inventory) {
              synchronized (records) {
                BorrowEntry borrowedBook = findRecord(rid);
                if (borrowedBook == null) {
                  pout.println(rid + " not found, no such borrow record");
                } else {
                  // borrow record found, remove record and update inventory
                  InventoryEntry libraryBook = findBook(borrowedBook.bookName);
                  libraryBook.quantity++;
                  records.remove(borrowedBook);
                  pout.println(rid + " is returned");
                }
              }
            }
          } else if (tokens[0].equals("list")) {
            String studentName = tokens[1];
            // search for all books that student borrowed
            List<String> studentsBorrows = new ArrayList<String>();
            boolean studentFound = false;
            for (BorrowEntry be : records) {
              if (be.studentName.equals(studentName)) {
                studentFound = true;
                studentsBorrows.add(be.recordID + " " + be.bookName);
              }
            }
            if (!studentFound) {
              pout.println("No record found for " + studentName);
            } else {
              pout.println(studentsBorrows.size());
              for (String s : studentsBorrows) {
                pout.println(s);
              }
            }
          } else if (tokens[0].equals("inventory")) {
            pout.println(inventory.size());
            for (InventoryEntry ie : inventory) {
              pout.println(ie.bookName + " " + ie.quantity);
            }
          } else if (tokens[0].equals("exit")) {
            // close connection and print inventory to inventory.txt
            pout.println(inventory.size());
            for (InventoryEntry ie : inventory) {
              pout.println(ie.bookName + " " + ie.quantity);
            }
            theClient.close();
            break;
          }
          pout.flush();
        }
      } catch (IOException e) {
        System.err.println(e);
      }
    }
  }

  private static InventoryEntry findBook(String bookName) {
    for (InventoryEntry ie : inventory) {
      if (ie.bookName.equals(bookName)) {
        return ie;
      }
    }
    return null;
  }

  private static BorrowEntry findRecord(int rid) {
    for (BorrowEntry be : records) {
      if (be.recordID == rid) {
        return be;
      }
    }
    return null;
  }
}
