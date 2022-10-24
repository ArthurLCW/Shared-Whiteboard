package util;

import client.DrawType;
import client.Position;
import tasks.SendChatMsg;
import tasks.SendClear;
import tasks.SendShape;
import tasks.SendText;

import java.awt.*;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;

public class ClearSender {
    private LinkedBlockingDeque<ID> userList;
    private ExecutorService pool;

    public ClearSender(LinkedBlockingDeque<ID> userList, ExecutorService pool){
        this.userList = userList;
        this.pool = pool;
    }

    public void sendClearMsg(){
        System.out.println("ClearSender: userList size "+userList.size());
        Iterator<ID> iterator = userList.iterator();
        while (iterator.hasNext()){
            ID id = iterator.next();
            pool.submit(new SendClear(id.getIP(), id.getPort()));
        }
    }
}
