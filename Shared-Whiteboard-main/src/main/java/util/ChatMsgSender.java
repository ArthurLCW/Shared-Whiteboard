package util;

import client.DrawType;
import client.Position;
import tasks.SendChatMsg;
import tasks.SendShape;
import tasks.SendText;

import java.awt.*;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;

public class ChatMsgSender {
    private LinkedBlockingDeque<ID> userList;
    private ExecutorService pool;
    private String senderName;
    private String chatMsg;

    public ChatMsgSender(LinkedBlockingDeque<ID> userList, ExecutorService pool, String senderName, String chatMsg){
        this.userList = userList;
        this.pool = pool;
        this.senderName = senderName;
        this.chatMsg = chatMsg;
    }

    public void sendChatMsg(){
        System.out.println("ChatMsgSender: userList size "+userList.size());
        Iterator<ID> iterator = userList.iterator();
        while (iterator.hasNext()){
            ID id = iterator.next();
            pool.submit(new SendChatMsg(id.getIP(), id.getPort(), senderName, chatMsg));
        }
    }
}
