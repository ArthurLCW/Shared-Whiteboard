package tasks;

import client.DrawBoard;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.util.Objects;


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
            String msgName = (String) command.get("MsgName");
            if (Objects.equals(msgName, "SendShape")) drawBoard.receiveShape(command);
            else if (Objects.equals(msgName, "SendText")) {
                drawBoard.receiveText(command);
            }

        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
