package tasks;


import org.json.simple.JSONObject;
import util.ID;
import util.Translator_IDQ_JSStr;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @desc update user-list with new user. Then send new user-list to every one on the list.
 * */
public class UpdateSendUsers {
    private Socket socket;
    private JSONObject command;
    private LinkedBlockingDeque<ID> userList; // server User List

    public UpdateSendUsers(Socket socket, JSONObject jsonObject, LinkedBlockingDeque<ID> userList) throws IOException {
        this.socket = socket;
        this.command = jsonObject;
        this.userList = userList;
        InetAddress userIP = this.socket.getInetAddress();
        this.socket.close(); // close receive sockets

        String username = (String) command.get("username");
        int userServerPort = ((Long) command.get("userServerPort")).intValue();
        ID id = new ID(username, userIP, userServerPort);
        this.userList.add(id);

        sendUpdates();
    }

    private void sendUpdates() throws IOException {
        Translator_IDQ_JSStr translator = new Translator_IDQ_JSStr();
        String message = translator.IDQueueToString(userList);

        Iterator<ID> iterator = userList.iterator();
        while (iterator.hasNext()){
            ID id = iterator.next();
            Socket updateSocket = new Socket(id.getIP(), id.getPort());
            DataInputStream input = new DataInputStream(updateSocket.getInputStream());
            DataOutputStream output = new DataOutputStream(updateSocket.getOutputStream());
            output.writeUTF(message);
            updateSocket.close();
        }
    }
}
