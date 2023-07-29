package util;

import client.DrawType;
import tasks.SendText;

import java.awt.*;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;

public class TextSender {
    private LinkedBlockingDeque<ID> userList;
    private ExecutorService pool;
    private Color color;
    private int posX;
    private int posY;
    private String str;


    public TextSender(int posX, int posY, String str, Color color, LinkedBlockingDeque<ID> userList,
                      ExecutorService pool){
        this.posX = posX;
        this.posY = posY;
        this.str = str;
        this.userList = userList;
        this.pool = pool;
        this.color = color;
    }

    public void sendText(){
        System.out.println("TextSender: userList size "+userList.size());
        Iterator<ID> iterator = userList.iterator();
        while (iterator.hasNext()){
            ID id = iterator.next();
            pool.submit(new SendText(posX, posY, str, color, id.getIP(), id.getPort(), pool));
        }
    }
}