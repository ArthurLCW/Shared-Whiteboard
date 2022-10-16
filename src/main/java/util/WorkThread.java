package util;

import client.DrawBoard;
import org.json.simple.parser.ParseException;
import util.ID;
import util.PeerReceiveSocket;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;


public class WorkThread extends Thread{
    private ExecutorService pool;
    private LinkedBlockingDeque<Socket> socketQueue;
    private LinkedBlockingDeque<ID> userList; // peer userList!
    private DrawBoard drawBoard;

    public WorkThread(ExecutorService pool, LinkedBlockingDeque<Socket> socketQueue, LinkedBlockingDeque<ID> userList,
                      DrawBoard drawBoard){
        this.pool = pool;
        this.socketQueue = socketQueue;
        this.userList = userList;
        this.drawBoard = drawBoard;
    }

    @Override
    public void run(){
        while (!isInterrupted()){
            while (!socketQueue.isEmpty()){
                Socket tempSocket = socketQueue.pop();
                try {
                    new PeerReceiveSocket(tempSocket, userList, drawBoard, pool);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (ParseException e) {
                    System.out.println(e.toString());
                }


            }
        }
    }
}
