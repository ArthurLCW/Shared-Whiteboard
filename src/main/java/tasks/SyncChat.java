package tasks;

import client.DrawBoard;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.swing.*;
import java.io.IOException;
import java.util.Objects;


public class SyncChat implements Runnable{
    private JSONObject command;
    private DefaultListModel chatRoomListModel;

    public SyncChat(JSONObject command, DefaultListModel chatRoomListModel){
        this.command = command;
        this.chatRoomListModel = chatRoomListModel;
    }

    @Override
    public void run() {
        String msg = command.get("senderName") + ": "+command.get("chatMsg");
        chatRoomListModel.addElement(msg);
    }
}
