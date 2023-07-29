package util;

import javax.net.ServerSocketFactory;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.LinkedBlockingDeque;
import java.net.Socket;


public class IOThread extends Thread{
    private LinkedBlockingDeque<Socket> socketQueue;
    private ServerSocket serverSocket;
    private int timeout;

    private ServerSocketFactory factory;

    public IOThread(int port, LinkedBlockingDeque<Socket> socketQueue, int timeout) throws IOException {
        this.timeout = timeout;
        this.socketQueue = socketQueue;
        serverSocket = new ServerSocket(port);
    }

    public void shutDown() throws IOException {
        serverSocket.close();
    }

    @Override
    public void run() {
        System.out.println("Server: IOThread: running! Waiting for connection...");
        while(!isInterrupted()) {
            try {
                Socket socket = serverSocket.accept();
                socket.setSoTimeout(this.timeout);
                socketQueue.add(socket);
                System.out.println("Server: IOThread: receive socket");
            } catch (IOException e) {
                System.out.println("Server: IOThread: "+e.toString());
                break;
            }
        }
        try {
            shutDown();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Server: IOThread: Quit. ");
    }
}
