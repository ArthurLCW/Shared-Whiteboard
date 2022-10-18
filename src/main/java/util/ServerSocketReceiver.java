package util;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingDeque;


public class ServerSocketReceiver {
    private Socket socket;
    private String MsgName;
    private JSONObject command;
    private LinkedBlockingDeque<ID> userList;


    public ServerSocketReceiver(Socket socket, LinkedBlockingDeque<ID> userList) throws IOException, ParseException {
        this.socket = socket;
        this.userList = userList;
        DataInputStream input = new DataInputStream(socket.getInputStream());
        DataOutputStream output = new DataOutputStream(socket.getOutputStream());
        String message = input.readUTF();
        JSONParser parser = new JSONParser();
        command = (JSONObject) parser.parse(message);
        MsgName = (String) command.get("MsgName");
    }

    public void response() throws IOException {
        if (Objects.equals(MsgName, "SendJoinInRequest")){
            UpdatedUserListSender updatedUserListSender = new UpdatedUserListSender(socket, command, userList);
            updatedUserListSender.sendUpdates();
        }
    }


}
