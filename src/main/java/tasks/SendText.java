package tasks;

import client.DrawType;
import client.Position;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Vector;
import java.util.concurrent.ExecutorService;

public class SendText implements Runnable
{
    private InetAddress ip;
    private int port;
    private int posX;
    private int posY;
    private String str;
    private ExecutorService pool;
    private Color color;

    public SendText(int posX, int posY, String str, Color color, InetAddress ip, int port, ExecutorService pool) {
        this.ip = ip;
        this.port = port;
        this.pool = pool;
        this.color = color;
        this.posX = posX;
        this.posY = posY;
        this.str = str;
    }

    /** Execute graphics send in thread pool */
    public void run() {
        ObjectMapper mapper = new ObjectMapper();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("MsgName", "SendText");

        JSONArray jsColorArray = new JSONArray();
        jsColorArray.add(color.getRed());
        jsColorArray.add(color.getGreen());
        jsColorArray.add(color.getBlue());

        jsonObject.put("posX", posX);
        jsonObject.put("posY", posY);
        jsonObject.put("str", str);
        jsonObject.put("colorVec", jsColorArray);
        String msg = jsonObject.toJSONString();
        System.out.println("SendText: "+msg);

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