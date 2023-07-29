package tasks;

import org.json.simple.JSONObject;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;


public class SendChatMsg implements Runnable
{
    private InetAddress ip;
    private int port;
    private String senderName;
    private String chatMsg;

    public SendChatMsg(InetAddress ip, int port, String senderName, String chatMsg) {
        this.ip = ip;
        this.port = port;
        this.chatMsg = chatMsg;
        this.senderName = senderName;
    }

    /** Execute graphics send in thread pool */
    public void run() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("MsgName", "SendChatMsg");
        jsonObject.put("senderName", senderName);
        jsonObject.put("chatMsg", chatMsg);
        String msg = jsonObject.toJSONString();
        System.out.println("SendChatMsg: "+msg);

        try {
            Socket socket = new Socket(ip, port);
            DataInputStream input = new DataInputStream(socket.getInputStream());
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());
            output.writeUTF(msg);
            socket.close(); // Close socket

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}