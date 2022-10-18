package server;
import org.json.simple.parser.ParseException;
import util.ID;
import util.IOThread;
import util.ServerSocketReceiver;

import java.net.InetAddress;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.LinkedBlockingDeque;

public class Server {

    // Declare the port number, username. ip is not defined.
    private static int port = 3200;
    private static String username = "admin";
    private static InetAddress serverIP;
    private static int timeout = 1000;


    public static void main(String[] args) throws IOException, ParseException {
        SimpleDateFormat ft = new SimpleDateFormat("hh:mm:ss");
        System.out.println("Main len: " + args.length);
        try {
            username = args[0];
            port = Integer.parseInt(args[1]);
            serverIP = InetAddress.getByName(args[2]);
        } catch (ArrayIndexOutOfBoundsException | UnknownHostException e) {
            System.out.println(e.toString());
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
                System.out.println("Server: received a connection!!! "+ft.format(new Date()));
                Socket socket = socketQueue.pop();
                ServerSocketReceiver serverSocketReceiver = new ServerSocketReceiver(socket, userList);
                serverSocketReceiver.response();
                System.out.println("Server: after execution, userList size: " + userList.size()+" " +ft.format(new Date()));
                socket.close();
            }
        }

    }
}
