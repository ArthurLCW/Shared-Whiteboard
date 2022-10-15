package util;

import client.DrawType;
import client.Position;
import tasks.SendShape;
import util.ID;

import java.awt.*;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;

public class ShapeSender {
    private Vector<Position> shapeVec;
    private LinkedBlockingDeque<ID> userList;
    private ExecutorService pool;
    private DrawType drawType;
    private Color color;

    public ShapeSender(Vector<Position> shapeVec, Color color, DrawType drawType, LinkedBlockingDeque<ID> userList, ExecutorService pool){
        this.shapeVec = new Vector(shapeVec);
        this.userList = userList;
        this.pool = pool;
        this.drawType = drawType;
        this.color = color;
    }

    public void sendShape(){
        System.out.println("ShapeSender: userList size "+userList.size());
        Iterator<ID> iterator = userList.iterator();
        while (iterator.hasNext()){
            ID id = iterator.next();
            pool.submit(new SendShape(color, drawType, id.getIP(), id.getPort(), shapeVec, pool));
        }
    }
}
