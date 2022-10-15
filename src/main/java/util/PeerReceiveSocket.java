package util;

import client.DrawBoard;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import tasks.SyncDraw;
import util.ID;
import util.Translator_IDQ_JSStr;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;

public class PeerReceiveSocket {
    private Socket socket;
    private String MsgName;
    private JSONObject command;
    private LinkedBlockingDeque<ID> userList;
    private DrawBoard drawBoard;
    private ExecutorService pool;


    public PeerReceiveSocket(Socket socket, LinkedBlockingDeque<ID> userList, DrawBoard drawBoard, ExecutorService pool) throws IOException, ParseException {
        this.socket = socket;
        this.userList = userList;
        this.drawBoard = drawBoard;
        this.pool = pool;
        DataInputStream input = new DataInputStream(socket.getInputStream());
        DataOutputStream output = new DataOutputStream(socket.getOutputStream());
        String message = input.readUTF();
        System.out.println("Peer: receive socket info: "+message);
        JSONParser parser = new JSONParser();
        command = (JSONObject) parser.parse(message);
        MsgName = (String) command.get("MsgName");
        react(message);

    }

    public void react(String message) throws ParseException, UnknownHostException {
        if (Objects.equals(MsgName, "UpdateSendUsers")){
            Translator_IDQ_JSStr translator = new Translator_IDQ_JSStr();
            Vector<ID> vecID = translator.StrToVec(message);
            System.out.println("Peer: receive vecSize: "+vecID.size());

            /** update userList according to vecID */
//            LinkedBlockingDeque<ID> IDQTemp = new LinkedBlockingDeque<ID>();
//            for (int i=0; i<vecID.size(); i++){
//                IDQTemp.add(vecID.elementAt(i));
//            }
//            System.out.println(userList);
//            userList = IDQTemp;
//            System.out.println(userList.size());
//            System.out.println(userList);

            userList.clear();
            for (int i=0; i<vecID.size(); i++){
                userList.add(vecID.elementAt(i));
            }
            System.out.println(userList);
            System.out.println(userList.size());
        }
        else if (Objects.equals(MsgName, "SendShape")){
            // drawBoard.getGraphics2D();
            // drawBoard.drawOthersPainting(command);
            pool.submit(new SyncDraw(command, drawBoard));

            System.out.println("Peers!");
        }

    }
}
