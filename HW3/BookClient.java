import java.util.Scanner;
import java.io.*;
import java.util.*;
import java.net.*;

public class BookClient {
  public static void main (String[] args) {
    String hostAddress;
    int tcpPort;
    int udpPort;
    int clientId;

    if (args.length != 2) {
      System.out.println("ERROR: Provide 2 arguments: commandFile, clientId");
      System.out.println("\t(1) <command-file>: file with commands to the server");
      System.out.println("\t(2) client id: an integer between 1..9");
      System.exit(-1);
    }

    String commandFile = args[0];
    clientId = Integer.parseInt(args[1]);
    hostAddress = "localhost";
    tcpPort = 7000;// hardcoded -- must match the server's tcp port
    udpPort = 8000;// hardcoded -- must match the server's udp port


    try {
      // TCP
      Socket server = new Socket(hostAddress, tcpPort);
      Scanner din = new Scanner(server.getInputStream());
      PrintStream pout = new PrintStream(server.getOutputStream());

      //UDP
      int len = 1024;
      byte[] rbuffer = new byte[len];
      DatagramPacket sPacket, rPacket;
      InetAddress ia = InetAddress.getByName(hostAddress);
      DatagramSocket udpSocket = new DatagramSocket();

      String activeProtocol = "U";  // Default is UDP
      Scanner sc = new Scanner(new FileReader(commandFile));

      while(sc.hasNextLine()) {
        String cmd = sc.nextLine();
        String[] tokens = cmd.split(" ");

        if (tokens[0].equals("setmode")) {
          // TODO: set the mode of communication for sending commands to the server
          activeProtocol = tokens[1];
          if (activeProtocol.equals("U")) {
            byte[] buffer = cmd.getBytes();
            sPacket = new DatagramPacket(buffer, buffer.length, ia, udpPort);
            udpSocket.send(sPacket);
            rPacket = new DatagramPacket(rbuffer, rbuffer.length);
            udpSocket.receive(rPacket);
            String result = new String(rPacket.getData(), 0,
                    rPacket.getLength());
            System.out.println(result);
          } else {
            pout.println(cmd);
            String result = din.nextLine();
            System.out.println(result);
          }
        }
        else if (tokens[0].equals("borrow")) {
          // TODO: send appropriate command to the server and display the
          // appropriate responses form the server
          if (activeProtocol.equals("U")) {
            byte[] buffer = cmd.getBytes();
            sPacket = new DatagramPacket(buffer, buffer.length, ia, udpPort);
            udpSocket.send(sPacket);
            rPacket = new DatagramPacket(rbuffer, rbuffer.length);
            udpSocket.receive(rPacket);
            String result = new String(rPacket.getData(), 0,
                    rPacket.getLength());
            System.out.println(result);
          } else {
            pout.println(cmd);
            String result = din.nextLine();
            System.out.println(result);
          }
        } else if (tokens[0].equals("return")) {
          // TODO: send appropriate command to the server and display the
          // appropriate responses form the server
          if (activeProtocol.equals("U")) {
            byte[] buffer = cmd.getBytes();
            sPacket = new DatagramPacket(buffer, buffer.length, ia, udpPort);
            udpSocket.send(sPacket);
            rPacket = new DatagramPacket(rbuffer, rbuffer.length);
            udpSocket.receive(rPacket);
            String result = new String(rPacket.getData(), 0,
                    rPacket.getLength());
            System.out.println(result);
          } else {
            pout.println(cmd);
            String result = din.nextLine();
            System.out.println(result);
          }
        } else if (tokens[0].equals("inventory")) {
          // TODO: send appropriate command to the server and display the
          // appropriate responses form the server
          if (activeProtocol.equals("U")) {
            byte[] buffer = cmd.getBytes();
            sPacket = new DatagramPacket(buffer, buffer.length, ia, udpPort);
            udpSocket.send(sPacket);
            rPacket = new DatagramPacket(rbuffer, rbuffer.length);
            udpSocket.receive(rPacket);
            String result = new String(rPacket.getData(), 0,
                    rPacket.getLength());
            String lines[] = result.split("\\r?\\n");
            int numResults = Integer.parseInt(lines[0]);
            for (int i = 0; i < numResults; i++) {
              System.out.println(lines[i+1]);
            }
          } else {
            pout.println(cmd);
            int numResults = Integer.parseInt(din.nextLine());
            for (int i = 0; i < numResults; i++) {
              System.out.println(din.nextLine());
            }
          }
        } else if (tokens[0].equals("list")) {
          // TODO: send appropriate command to the server and display the
          // appropriate responses form the server
          if (activeProtocol.equals("U")) {
            byte[] buffer = cmd.getBytes();
            sPacket = new DatagramPacket(buffer, buffer.length, ia, udpPort);
            udpSocket.send(sPacket);
            rPacket = new DatagramPacket(rbuffer, rbuffer.length);
            udpSocket.receive(rPacket);
            String result = new String(rPacket.getData(), 0,
                    rPacket.getLength());
            String lines[] = result.split("\\r?\\n");
            int numResults = Integer.parseInt(lines[0]);
            for (int i = 0; i < numResults; i++) {
              System.out.println(lines[i+1]);
            }
          } else {
            pout.println(cmd);
            int numResults = Integer.parseInt(din.nextLine());
            for (int i = 0; i < numResults; i++) {
              System.out.println(din.nextLine());
            }
          }
        } else if (tokens[0].equals("exit")) {
          // TODO: send appropriate command to the server
          if (activeProtocol.equals("U")) {
            byte[] buffer = cmd.getBytes();
            sPacket = new DatagramPacket(buffer, buffer.length, ia, udpPort);
            udpSocket.send(sPacket);
            rPacket = new DatagramPacket(rbuffer, rbuffer.length);
            udpSocket.receive(rPacket);
            String result = new String(rPacket.getData(), 0,
                    rPacket.getLength());
            String lines[] = result.split("\\r?\\n");
            int numResults = Integer.parseInt(lines[0]);
            for (int i = 0; i < numResults; i++) {
              System.out.println("inventory.txt:" + lines[i+1]);
            }
          } else {
            pout.println(cmd);
            int numResults = Integer.parseInt(din.nextLine());
            for (int i = 0; i < numResults; i++) {
              System.out.println("inventory.txt:" + din.nextLine());
            }
          }
        } else {
          System.out.println("ERROR: No such command");
        }
        pout.flush();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
