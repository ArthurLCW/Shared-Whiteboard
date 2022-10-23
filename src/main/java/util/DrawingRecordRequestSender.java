package util;

import org.json.simple.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingDeque;

/** Notice: only use this class after the join in request has been proved */
public class DrawingRecordRequestSender {
    private LinkedBlockingDeque<ID> userList;
    private JSONObject jsonObject;

    public DrawingRecordRequestSender(LinkedBlockingDeque<ID> userList){
        this.userList = userList;
    }
    public void send() throws IOException {
        jsonObject = new JSONObject();
        ID id = userList.getFirst();
        Socket socket = new Socket(id.getIP(), id.getPort());
        DataInputStream input = new DataInputStream(socket.getInputStream());
        DataOutputStream output = new DataOutputStream(socket.getOutputStream());
        output.writeUTF(encode());
        output.flush();
        socket.close();
    }

    private String encode(){
        jsonObject.put("MsgName", "DrawingRecordRequest");
        return jsonObject.toJSONString();
    }
}
