package util;

import client.DrawBoard;
import client.ManagerFlag;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import tasks.SyncChat;
import tasks.SyncDraw;

import javax.swing.*;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Objects;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;

public class PeerSocketReceiver {
    private Socket socket;
    private String MsgName;
    private JSONObject command;
    private LinkedBlockingDeque<ID> userList;
    private DrawBoard drawBoard;
    private ExecutorService pool;
    private String message;
    private JFrame frame;
    private ID managerInfo;
    private DataInputStream input;
    private DataOutputStream output;
    private InetAddress serverIP;
    private int serverPort;
    private LinkedBlockingDeque<String> drawingRecord;
    private DefaultListModel getUserlistModel;
    private DefaultListModel chatRoomListModel;
    private ManagerFlag managerFlag;


    public PeerSocketReceiver(Socket socket, LinkedBlockingDeque<ID> userList, DrawBoard drawBoard,
                              ExecutorService pool, JFrame frame, ID managerInfo, InetAddress serverIP, int serverPort,
                              LinkedBlockingDeque<String> drawingRecord, DefaultListModel getUserlistModel,
                              ManagerFlag managerFlag, DefaultListModel chatRoomListModel)
            throws IOException, ParseException {
        this.socket = socket;
        this.userList = userList;
        this.drawBoard = drawBoard;
        this.pool = pool;
        this.frame = frame;
        this.managerInfo = managerInfo;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.drawingRecord = drawingRecord;
        this.getUserlistModel = getUserlistModel;
        this.managerFlag = managerFlag;
        this.chatRoomListModel =  chatRoomListModel;
        input = new DataInputStream(socket.getInputStream());
        output = new DataOutputStream(socket.getOutputStream());
        message = input.readUTF();
        System.out.println("Peer: receive socket info: "+message);
        JSONParser parser = new JSONParser();
        command = (JSONObject) parser.parse(message);
        MsgName = (String) command.get("MsgName");
    }

    public void response() throws ParseException, IOException {
        if (Objects.equals(MsgName, "UpdateSendUsers")){
            Translator_IDQ_JSStr translator = new Translator_IDQ_JSStr();
            Vector<ID> vecID = translator.StrToVec(message);

            userList.clear();
            getUserlistModel.clear();
            for (int i=0; i<vecID.size(); i++){
                userList.add(vecID.elementAt(i));
                getUserlistModel.addElement(vecID.elementAt(i).getUsername()+" "+vecID.elementAt(i).getIP()+" "+
                        vecID.elementAt(i).getPort());
            }
            System.out.println("Peer userList size: "+userList.size());
            if ((userList.size()==1) && (managerInfo.getPort()==-1)){ // now the peer is the manager
                JOptionPane.showMessageDialog(frame,
                        "You are the manager. ");
                managerInfo.setUsername(vecID.elementAt(0).getUsername());
                managerInfo.setIP(vecID.elementAt(0).getIP());
                managerInfo.setPort(vecID.elementAt(0).getPort());
                managerFlag.setFlag(true);
            }
//            getUserlistModel.clear();
//            for (int i=0; i< vecID.size(); i++){
//                getUserlistModel.addElement(vecID.elementAt(i).getUsername()+" "+vecID.elementAt(i).getIP()+" "+
//                        vecID.elementAt(i).getPort());
//            }

        }
        else if ((Objects.equals(MsgName, "SendShape")) || (Objects.equals(MsgName, "SendText"))){
            pool.submit(new SyncDraw(command, drawBoard));
            System.out.println("Peers sending!");
        }
        else if (Objects.equals(MsgName, "PermissionRequest")){
            String username = (String) command.get("username");
            int userServerPort = ((Long) command.get("userServerPort")).intValue();
            String strIP = (String) command.get("userIP");
            InetAddress ip = InetAddress.getByName(strIP);
            System.out.println("client want permit: "+username+" "+strIP+" "+userServerPort);
            int decision = JOptionPane.showOptionDialog(frame,
                    "Do you want to give "+ username+" with ip: "+strIP+" port: "+userServerPort+" to access the whiteboard?",
                    "New User Join In Request",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,null,null); //default button title

            if (decision == JOptionPane.YES_OPTION) {
                // tell server you agree to give access
                JSONObject permit = new JSONObject();
                permit.put("MsgName", "GrantAccess");
                permit.put("username", username);
                permit.put("userIP", strIP);
                permit.put("userServerPort", userServerPort);
                String permitStr = permit.toJSONString();
                Socket permissionSocket = new Socket(serverIP, serverPort);
                DataInputStream inputP = new DataInputStream(permissionSocket.getInputStream());
                DataOutputStream outputP = new DataOutputStream(permissionSocket.getOutputStream());
                outputP.writeUTF(permitStr);
                permissionSocket.close();

                // P2P transmit history records
                JSONObject sendRecords = new JSONObject();
                sendRecords.put("MsgName", "SyncHistoryRecord");
                JSONArray records = new JSONArray();

                Iterator<String> iterator = drawingRecord.iterator();
                while (iterator.hasNext()){
                    String singleRecord = iterator.next();
                    records.add(singleRecord);
                }
                sendRecords.put("DrawingRecord", records);
                String sendRecordsStr = sendRecords.toJSONString();
                Socket syncRecordSocket = new Socket(ip, userServerPort);
                DataOutputStream outputS = new DataOutputStream(syncRecordSocket.getOutputStream());
                outputS.writeUTF(sendRecordsStr);
                syncRecordSocket.close();
            }
            else if (decision == JOptionPane.NO_OPTION) {
                // tell the client he is rejected. close his window
                JSONObject deny = new JSONObject();
                deny.put("MsgName", "DenyAccess");
                String denyStr = deny.toJSONString();
                Socket denySocket = new Socket(ip, userServerPort);
                DataOutputStream outputP = new DataOutputStream(denySocket.getOutputStream());
                outputP.writeUTF(denyStr);
                denySocket.close();
            }
            else {
                // the user closed the dialog without clicking an button
            }
        }
        else if (Objects.equals(MsgName, "DenyAccess")){
            int decision = JOptionPane.showConfirmDialog(frame, "Sorry! Your access request has been denied. ",
                    "Manager Message", JOptionPane.DEFAULT_OPTION);
            if (decision==JOptionPane.OK_OPTION){
                frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
            }
        }
        else if (Objects.equals(MsgName, "Dismiss")){
            System.out.println("Peer: Dismiss");
            int decision = JOptionPane.showConfirmDialog(frame, "Sorry! Manager left. The shared whiteboard is dismissed. ",
                    "Manager Message", JOptionPane.DEFAULT_OPTION);
            if (decision==JOptionPane.OK_OPTION){
                frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
            }
        }
        else if (Objects.equals(MsgName, "KickOut")){
            System.out.println("Peer: KickOut");
            int decision = JOptionPane.showConfirmDialog(frame, "Sorry! You are kicked out by manager. ",
                    "Manager Message", JOptionPane.DEFAULT_OPTION);
            if (decision==JOptionPane.OK_OPTION){
                frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
            }
        }
        else if (Objects.equals(MsgName, "SyncHistoryRecord")){
            System.out.println("Peer: SyncHistoryRecord");
            JSONParser parser = new JSONParser();
            JSONArray records = (JSONArray) command.get("DrawingRecord");
            for (int i=0; i<records.size(); i++){
                String str = (String) records.get(i);
                JSONObject jsobj = (JSONObject) parser.parse(str);
                pool.submit(new SyncDraw(jsobj, drawBoard));
            }
        }
        else if ((Objects.equals(MsgName, "SendChatMsg"))){
            pool.submit(new SyncChat(command, chatRoomListModel));
            System.out.println("Peers sending!");
        }
    }
}
