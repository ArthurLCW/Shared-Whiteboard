package tasks;

import client.DrawType;
import client.Position;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import util.ID;

import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;

public class SendShape implements Runnable
{
    private InetAddress ip;
    private int port;
    private Vector<Position> shapeVec;
    private ExecutorService pool;
    private DrawType drawType;
    private Color color;

    public SendShape(Color color, DrawType drawType, InetAddress ip, int port, Vector<Position> shapeVec, ExecutorService pool) {
        this.ip = ip;
        this.port = port;
        this.drawType = drawType;
        this.shapeVec = shapeVec;
        this.pool = pool;
        this.color = color;
    }

    /** Execute graphics send in thread pool */
    public void run() {
        ObjectMapper mapper = new ObjectMapper();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("MsgName", "SendShape");
        try {
            jsonObject.put("drawType", mapper.writeValueAsString(drawType));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        JSONArray jsArray = new JSONArray();
        for (int i=0; i<shapeVec.size();i++){
            try {
                jsArray.add(mapper.writeValueAsString(shapeVec.elementAt(i)));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        JSONArray jsColorArray = new JSONArray();
        jsColorArray.add(color.getRed());
        jsColorArray.add(color.getGreen());
        jsColorArray.add(color.getBlue());


        jsonObject.put("shapeVec", jsArray);
        jsonObject.put("colorVec", jsColorArray);
        String msg = jsonObject.toJSONString();
        System.out.println("SendShape: "+msg);

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