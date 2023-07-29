package util;


import org.json.simple.JSONObject;
import util.ID;
import util.Translator_IDQ_JSStr;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @desc update user-list with new user. Then send new user-list to every one on the list.
 * */
public class UpdatedUserListSender {
    private Socket socket;
    private JSONObject command;
    private LinkedBlockingDeque<ID> userList; // server User List
    private ID manager;
    private boolean managerRun = false;

    public UpdatedUserListSender(Socket socket, JSONObject jsonObject, LinkedBlockingDeque<ID> userList, ID manager) throws IOException {
        this.manager = manager;
        this.socket = socket;
        this.command = jsonObject;
        this.userList = userList;
        InetAddress userIP = this.socket.getInetAddress();
        this.socket.close(); // close receive sockets




        if (Objects.equals((String) command.get("MsgName"), "SendJoinInRequest")){
            String username = (String) command.get("username");
            int userServerPort = ((Long) command.get("userServerPort")).intValue();
            ID id = new ID(username, userIP, userServerPort);

            if (userList.isEmpty()){
                this.manager.setUsername(username);
                this.manager.setIP(userIP);
                this.manager.setPort(userServerPort);
            }

            this.userList.add(id);
        }
        else if (Objects.equals((String) command.get("MsgName"), "SendLeaveRequest")){ // todo: close all if manager
            String username = (String) command.get("username");
            int userServerPort = ((Long) command.get("userServerPort")).intValue();

            if (Objects.equals(username, manager.getUsername()) && Objects.equals(userIP.getHostAddress(), manager.getIP().getHostAddress())
            && (userServerPort == manager.getPort())){ // the manager runs!
                managerRun = true;
            }
            System.out.println("ManagerRun: "+managerRun);

            Iterator iteratorVals = this.userList.iterator();
            while (iteratorVals.hasNext()){
                ID id = (ID) iteratorVals.next();
                if (Objects.equals(username,id.getUsername()) && Objects.equals(userIP,id.getIP())
                        && userServerPort==id.getPort()){
                    this.userList.remove(id);
                }
            }

        }

        else if (Objects.equals((String) command.get("MsgName"), "KickOut")){
            String username = (String) command.get("username");
            int userServerPort = ((Long) command.get("userServerPort")).intValue();
            InetAddress ip = InetAddress.getByName((String) command.get("userIP"));

            Iterator iteratorVals = this.userList.iterator();
            while (iteratorVals.hasNext()){
                ID id = (ID) iteratorVals.next();
                if (Objects.equals(username,id.getUsername()) && Objects.equals(ip,id.getIP())
                        && userServerPort==id.getPort()){
                    this.userList.remove(id);
                }
            }
        }

        else if (Objects.equals((String) command.get("MsgName"), "GrantAccess")){
            String username = (String) command.get("username");
            int userServerPort = ((Long) command.get("userServerPort")).intValue();
            userIP = InetAddress.getByName((String) command.get("userIP"));
            ID id = new ID(username, userIP, userServerPort);

            if (userList.isEmpty()){
                this.manager.setUsername(username);
                this.manager.setIP(userIP);
                this.manager.setPort(userServerPort);
            }
            this.userList.add(id);
        }
    }

    public void sendUpdates() throws IOException {
        Translator_IDQ_JSStr translator = new Translator_IDQ_JSStr();
        String message = translator.IDQueueToString(userList);

        Iterator<ID> iterator = userList.iterator();
        while (iterator.hasNext()){
            ID id = iterator.next();
            Socket updateSocket = new Socket(id.getIP(), id.getPort());
            DataInputStream input = new DataInputStream(updateSocket.getInputStream());
            DataOutputStream output = new DataOutputStream(updateSocket.getOutputStream());

            if (!managerRun){
                output.writeUTF(message);
            }
            else if (managerRun){
                System.out.println("The manager runs!!!");
                JSONObject dismiss = new JSONObject();
                dismiss.put("MsgName", "Dismiss");
                String dismissStr = dismiss.toJSONString();
                output.writeUTF(dismissStr);
            }


            updateSocket.close();
        }
    }
}
