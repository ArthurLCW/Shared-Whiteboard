package tasks;

import client.DrawBoard;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;


public class SyncDraw implements Runnable{
    private JSONObject command;
    private DrawBoard drawBoard;

    public SyncDraw(JSONObject command, DrawBoard drawBoard){
        this.command = command;
        this.drawBoard = drawBoard;
    }

    @Override
    public void run() {
        try {
            drawBoard.drawOthersPainting(command);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
