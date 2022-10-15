package server;
import org.json.simple.parser.ParseException;
import util.ID;
import util.IOThread;
import util.ServerReceiveSockets;

import java.net.InetAddress;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.LinkedBlockingDeque;

public class Server {

    // Declare the port number, username. ip is not defined.
    private static int port = 4321;
    private static String username = "admin";
    private static InetAddress serverIP;
    private static int timeout = 1000;


    public static void main(String[] args) throws IOException, ParseException {
        System.out.println("Main len: " + args.length);
        try {
            username = args[0];
            port = Integer.parseInt(args[1]);
            serverIP = InetAddress.getByName(args[2]);
        } catch (ArrayIndexOutOfBoundsException | UnknownHostException e) {
            System.out.println(e.getStackTrace());
        }
        LinkedBlockingDeque<Socket> socketQueue = new LinkedBlockingDeque<Socket>();
        LinkedBlockingDeque<ID> userList = new LinkedBlockingDeque<ID>();
        IOThread ioThread = null; // used to receive all sockets and store in a queue
        try {
            ioThread = new IOThread(port, socketQueue, timeout);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ioThread.start();

        while (true) {
            while (!socketQueue.isEmpty()) {
                System.out.println("Server: received a connection!!!");
                Socket socket = socketQueue.pop();
                new ServerReceiveSockets(socket, userList);
                System.out.println("Server: after execution, userList size: " + userList.size());
                socket.close();
            }
        }

    }
}
