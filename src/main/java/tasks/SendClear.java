package tasks;

import org.json.simple.JSONObject;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;


public class SendClear implements Runnable
{
    private InetAddress ip;
    private int port;

    public SendClear(InetAddress ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    /** Execute graphics send in thread pool */
    public void run() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("MsgName", "SendClear");
        String msg = jsonObject.toJSONString();
        System.out.println("SendClear");

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