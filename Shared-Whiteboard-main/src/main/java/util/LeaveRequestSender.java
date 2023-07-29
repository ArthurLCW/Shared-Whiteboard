package util;

import org.json.simple.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class LeaveRequestSender {
    private static String username;
    private static int userServerPort;
    private static JSONObject jsonObject;
    private static Socket socket;

    public LeaveRequestSender(Socket socket, String username, int userServerPort) {
        jsonObject = new JSONObject();
        this.username = username;
        this.userServerPort = userServerPort;
        this.socket = socket;
    }
    public static void send() throws IOException {
        DataInputStream input = new DataInputStream(socket.getInputStream());
        DataOutputStream output = new DataOutputStream(socket.getOutputStream());
        output.writeUTF(encode());
        output.flush();
        socket.close();
    }

    public static String encode(){
        jsonObject.put("MsgName", "SendLeaveRequest");
        jsonObject.put("username", username);
        jsonObject.put("userServerPort", Integer.valueOf(userServerPort));
        return jsonObject.toJSONString();
    }
}
