package util;

import org.json.simple.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class PermissionRequestSender {
    private String username;
    private int userServerPort;
    private JSONObject jsonObject;
    private Socket socket;
    private InetAddress ip;
    private ID manager;
    private JSONObject command; // command sent by client

    public PermissionRequestSender(Socket socket, JSONObject command, ID manager){
        jsonObject = new JSONObject();
        this.username = (String) command.get("username");
        this.userServerPort = ((Long) command.get("userServerPort")).intValue();
        this.socket = socket;
        this.ip = this.socket.getInetAddress();
        this.manager = manager;
    }

    public void send() throws IOException {
//        DataInputStream input = new DataInputStream(socket.getInputStream());
//        DataOutputStream output = new DataOutputStream(socket.getOutputStream());
//        output.writeUTF(encode());
//        output.flush();
        socket.close();
        Socket askManagerSocket = new Socket(manager.getIP(), manager.getPort());
        DataInputStream input = new DataInputStream(askManagerSocket.getInputStream());
        DataOutputStream output = new DataOutputStream(askManagerSocket.getOutputStream());
        output.writeUTF(encode());
        askManagerSocket.close();
    }

    public String encode(){
        jsonObject.put("MsgName", "PermissionRequest");
        jsonObject.put("username", username);
        jsonObject.put("userServerPort", Integer.valueOf(userServerPort));
        jsonObject.put("userIP", ip.getHostAddress());
        return jsonObject.toJSONString();
    }
}
