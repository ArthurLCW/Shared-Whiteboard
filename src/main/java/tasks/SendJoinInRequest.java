package tasks;

import org.json.simple.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class SendJoinInRequest {
    private String username;
    private int userServerPort;
    private JSONObject jsonObject;
    private Socket socket;

    public SendJoinInRequest(Socket socket, String username, int userServerPort) throws IOException {
        jsonObject = new JSONObject();
        this.username = username;
        this.userServerPort = userServerPort;
        this.socket = socket;
        
        DataInputStream input = new DataInputStream(socket.getInputStream());
        DataOutputStream output = new DataOutputStream(socket.getOutputStream());
        output.writeUTF(encode());
        output.flush();
        socket.close();
    }

    public String encode(){
        jsonObject.put("MsgName", "SendJoinInRequest");
        jsonObject.put("username", username);
        jsonObject.put("userServerPort", Integer.valueOf(userServerPort));
        return jsonObject.toJSONString();
    }
}
