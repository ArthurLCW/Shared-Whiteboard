package util;

import client.DrawBoard;
import client.ManagerFlag;
import org.json.simple.parser.ParseException;

import javax.swing.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;


public class WorkThread extends Thread{
    private ExecutorService pool;
    private LinkedBlockingDeque<Socket> socketQueue;
    private LinkedBlockingDeque<ID> userList; // peer userList!
    private DrawBoard drawBoard;
    private JFrame frame;
    private ID managerInfo;
    private InetAddress serverIP;
    private int serverPort;
    private LinkedBlockingDeque<String> drawingRecord;
    private DefaultListModel getUserlistModel;
    private ManagerFlag managerFlag;

    public WorkThread(ExecutorService pool, LinkedBlockingDeque<Socket> socketQueue, LinkedBlockingDeque<ID> userList,
                      DrawBoard drawBoard, JFrame frame, ID managerInfo, InetAddress serverIP, int serverPort,
                      LinkedBlockingDeque<String> drawingRecord, DefaultListModel getUserlistModel,
                      ManagerFlag managerFlag){
        this.pool = pool;
        this.socketQueue = socketQueue;
        this.userList = userList;
        this.drawBoard = drawBoard;
        this.frame = frame;
        this.managerInfo = managerInfo;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.drawingRecord = drawingRecord;
        this.getUserlistModel = getUserlistModel;
        this.managerFlag = managerFlag;
    }

    @Override
    public void run(){
        while (!isInterrupted()){
            while (!socketQueue.isEmpty()){
                Socket tempSocket = socketQueue.pop();
                try {
                    PeerSocketReceiver peerSocketReceiver = new PeerSocketReceiver(tempSocket, userList, drawBoard, pool,
                            frame, managerInfo, serverIP, serverPort, drawingRecord, getUserlistModel, managerFlag);
                    peerSocketReceiver.response();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (ParseException e) {
                    System.out.println(e.toString());
                }


            }
        }
    }
}
