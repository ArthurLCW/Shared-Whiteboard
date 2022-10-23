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

    private ID manager;


    public ServerSocketReceiver(Socket socket, LinkedBlockingDeque<ID> userList, ID manager) throws IOException, ParseException {
        this.socket = socket;
        this.userList = userList;
        this.manager = manager;
        DataInputStream input = new DataInputStream(socket.getInputStream());
        DataOutputStream output = new DataOutputStream(socket.getOutputStream());
        String message = input.readUTF();
        JSONParser parser = new JSONParser();
        command = (JSONObject) parser.parse(message);
        MsgName = (String) command.get("MsgName");
    }

    public void response() throws IOException {
        if (Objects.equals(MsgName, "SendJoinInRequest")){
            if (userList.size()==0){
                UpdatedUserListSender updatedUserListSender = new UpdatedUserListSender(socket, command, userList, manager);
                updatedUserListSender.sendUpdates();
            }
            else{
                PermissionRequestSender permissionRequestSender = new PermissionRequestSender(socket, command, manager);
                permissionRequestSender.send();
            }

        }else if (Objects.equals(MsgName, "SendLeaveRequest")){
            UpdatedUserListSender updatedUserListSender = new UpdatedUserListSender(socket, command, userList, manager);
            updatedUserListSender.sendUpdates();
        }

        else if (Objects.equals(MsgName, "GrantAccess")){
            UpdatedUserListSender updatedUserListSender = new UpdatedUserListSender(socket, command, userList, manager);
            updatedUserListSender.sendUpdates();
        }
    }


}
