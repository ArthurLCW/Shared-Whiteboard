package client;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import util.ID;
import util.ShapeSender;
import util.TextSender;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;

public class DrawBoard extends JPanel {
    private Image image;
    private Graphics2D graphics2D;
    private Position posOld = new Position(-1,-1), posCur = new Position(-1,-1);
    // The above 2 positions are solely used for free hand drawing.
    private Position posStart = new Position(-1,-1), posEnd = new Position(-1,-1);
    // The above 2 positions are used for shapes drawing.
    private Position posThird = new Position(-1,-1);
    // The above position is only used for triangle;


    private DrawType drawType=DrawType.HandFree;
    private Color drawColor = Color.black;
    private ExecutorService pool;
    private LinkedBlockingDeque<ID> userList;
    public LinkedBlockingDeque<String> drawingRecord;

    private Vector<Position> freeVec = new Vector<Position>();
    private Vector<Position> shapeVec = new Vector<Position>(2);


    public DrawBoard(ExecutorService pool, LinkedBlockingDeque<ID> userList) {
        this.pool = pool;
        this.userList = userList;
        this.drawingRecord = new LinkedBlockingDeque<String>();

        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (drawType == DrawType.HandFree) posOld.setXY(e.getX(), e.getY());
                else if (drawType == DrawType.Text) {
                    Object[] options = {"Confirm", "Cancel"};
                    JPanel textEnteringPanel = new JPanel();
                    JTextField textInput = new JTextField(10);
                    textEnteringPanel.add(new JLabel("The following text will be displayed on the whiteboard: "));
                    textEnteringPanel.add(textInput);

                    int result = JOptionPane.showOptionDialog(null, textEnteringPanel,
                            "Text Entering Box", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                            null, options, options[0]);
                    if (result==JOptionPane.OK_OPTION){
                        // System.out.println("Text: "+textInput.getText()+" pos "+e.getX()+" "+e.getY());
                        graphics2D.drawString(textInput.getText(), e.getX(), e.getY());
                        TextSender textSender = new TextSender(e.getX(),e.getY(),textInput.getText(),drawColor,
                                userList, pool);
                        textSender.sendText();
                        repaint();
                    }
                }
                else {
                    posStart.setXY(e.getX(), e.getY());
                    posEnd.updateXY(posStart);
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (drawType == DrawType.HandFree){
                    posCur.setXY(e.getX(), e.getY());
                    posCur.boundaryCheckPos();
                    freeVec.add(new Position(posOld));
                    graphics2D.drawLine(posOld.getX(), posOld.getY(), posCur.getX(), posCur.getY());
                    // System.out.println("HandFree: "+ posOld.getX()+" "+ posOld.getY()+" "+ posCur.getX()+" "+ posCur.getY());
                    Vector<Position> vec = new Vector<Position>();
                    vec.add(new Position(posOld));
                    vec.add(new Position(posCur));
                    ShapeSender shapeSender = new ShapeSender(vec,drawColor,DrawType.Line, userList, pool);
                    shapeSender.sendShape();

                    posOld.updateXY(posCur);
                    repaint();
                }

            }
        });
        addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                if (drawType == DrawType.HandFree) {
                    posCur.setXY(e.getX(), e.getY());
                    freeVec.add(new Position(posCur));
                    freeVec.clear();
                }
                else if (drawType == DrawType.Text) {}//TODO : implement This!
                else {
                    if (shapeVec.size()==0){
                        posEnd.setXY(e.getX(), e.getY());
                        shapeVec.add(new Position(posStart));
                        shapeVec.add(new Position(posEnd));
                    }
                    else if (shapeVec.size()==2){
                        posThird.setXY(e.getX(), e.getY());
                        shapeVec.add(new Position(posThird));
                    }

                    ShapeSender shapeSender;
                    switch (drawType){
                        case Line:
                            graphics2D.drawLine(posStart.getX(), posStart.getY(), posEnd.getX(), posEnd.getY());
                            shapeSender = new ShapeSender(shapeVec,drawColor,drawType, userList, pool);
                            shapeSender.sendShape();
                            shapeVec.clear();
                            break;
                        case Circle:
                            int CircleCentreX = (posStart.getX()+posEnd.getX())/2;
                            int CircleCentreY = (posStart.getY()+posEnd.getY())/2;
                            int diameter = (int)posStart.distance(posEnd);
                            graphics2D.drawOval(CircleCentreX-diameter/2, CircleCentreY-diameter/2, diameter, diameter);
                            shapeSender = new ShapeSender(shapeVec,drawColor,drawType, userList, pool);
                            shapeSender.sendShape();
                            shapeVec.clear();
                            break;
                        case Triangle:
                            if (shapeVec.size()==3){
                                graphics2D.drawLine(shapeVec.elementAt(0).getX(), shapeVec.elementAt(0).getY(),
                                        shapeVec.elementAt(1).getX(), shapeVec.elementAt(1).getY());
                                graphics2D.drawLine(shapeVec.elementAt(0).getX(), shapeVec.elementAt(0).getY(),
                                        shapeVec.elementAt(2).getX(), shapeVec.elementAt(2).getY());
                                graphics2D.drawLine(shapeVec.elementAt(1).getX(), shapeVec.elementAt(1).getY(),
                                        shapeVec.elementAt(2).getX(), shapeVec.elementAt(2).getY());
                                shapeSender = new ShapeSender(shapeVec,drawColor,drawType, userList, pool);
                                shapeSender.sendShape();
                                shapeVec.clear();
                            }
                            break;
                        case Rectangle:
                            graphics2D.drawRect(Math.min(posStart.getX(), posEnd.getX()), Math.min(posStart.getY(), posEnd.getY()),
                                    Math.abs(posStart.getX()-posEnd.getX()), Math.abs(posStart.getY()-posEnd.getY()));
                            shapeSender = new ShapeSender(shapeVec,drawColor,drawType, userList, pool);
                            shapeSender.sendShape();
                            shapeVec.clear();
                            break;
                        }
                }
                repaint();
            }
        });
    }

    protected void paintComponent(Graphics graphics) {
        if (image == null) {
            image = createImage(getSize().width, getSize().height);
            graphics2D = (Graphics2D) image.getGraphics();
            System.out.println("Init type: "+drawType.toString());
            clear();
        }
        graphics.drawImage(image, 0, 0, null);
    }

    public void clear() {
        graphics2D.setPaint(Color.white);
        graphics2D.fillRect(0, 0, getSize().width, getSize().height );
        repaint();
        graphics2D.setPaint(Color.black);
        System.out.println("clear board");
        drawingRecord.clear();
    }

    public void setType(DrawType drawType){
        this.drawType = drawType;
        System.out.println("Draw type is set to be "+drawType.toString());
        shapeVec.clear();
    }

    public void setColor(Color color){
        this.drawColor = color;
        graphics2D.setColor(drawColor);
        System.out.println("Draw color is set to be "+drawColor.getRed()+" "+drawColor.getGreen()+" "+drawColor.getBlue());
    }


    public void receiveShape(JSONObject jsonObject) throws ParseException, IOException {
        drawingRecord.add(jsonObject.toJSONString());

        String drawTypeStr = (String) jsonObject.get("drawType");
        JSONArray colorArray = (JSONArray) jsonObject.get("colorVec");
        int R = ((Long) colorArray.get(0)).intValue();
        int G = ((Long) colorArray.get(1)).intValue();
        int B = ((Long) colorArray.get(2)).intValue();
        Color color = new Color(R,G,B);
        JSONArray shapeVec = (JSONArray) jsonObject.get("shapeVec");

        Vector<Position> vecP = new Vector<Position>();
        for (int i=0; i<shapeVec.size(); i++){
            String str = (String) shapeVec.get(i);
            JSONParser parser  = new JSONParser();
            JSONObject jsPos = (JSONObject) parser.parse(str);
            Position pos = new Position(((Long)jsPos.get("x")).intValue(), ((Long)jsPos.get("y")).intValue());
            vecP.add(pos);
        }

        Graphics2D g2Temp = (Graphics2D) image.getGraphics();
        g2Temp.setColor(color);

        switch (drawTypeStr){
            case "\"Line\"":
                g2Temp.drawLine(vecP.elementAt(0).getX(), vecP.elementAt(0).getY(),
                        vecP.elementAt(1).getX(), vecP.elementAt(1).getY());
                break;
            case "\"Circle\"":
                int CircleCentreX = (vecP.elementAt(0).getX()+vecP.elementAt(1).getX())/2;
                int CircleCentreY = (vecP.elementAt(0).getY()+vecP.elementAt(1).getY())/2;
                int diameter = (int)vecP.elementAt(0).distance(vecP.elementAt(1));
                g2Temp.drawOval(CircleCentreX-diameter/2, CircleCentreY-diameter/2, diameter, diameter);
                break;
            case "\"Rectangle\"":
                g2Temp.drawRect(Math.min(vecP.elementAt(0).getX(), vecP.elementAt(1).getX()),
                        Math.min(vecP.elementAt(0).getY(), vecP.elementAt(1).getY()),
                        Math.abs(vecP.elementAt(0).getX()-vecP.elementAt(1).getX()),
                        Math.abs(vecP.elementAt(0).getY()-vecP.elementAt(1).getY()));
                break;
            case "\"Triangle\"":
                g2Temp.drawLine(vecP.elementAt(0).getX(), vecP.elementAt(0).getY(),
                        vecP.elementAt(1).getX(), vecP.elementAt(1).getY());
                g2Temp.drawLine(vecP.elementAt(0).getX(), vecP.elementAt(0).getY(),
                        vecP.elementAt(2).getX(), vecP.elementAt(2).getY());
                g2Temp.drawLine(vecP.elementAt(1).getX(), vecP.elementAt(1).getY(),
                        vecP.elementAt(2).getX(), vecP.elementAt(2).getY());
                break;
        }
        repaint();
    }

    public void receiveText(JSONObject jsonObject) throws ParseException{
        drawingRecord.add(jsonObject.toJSONString());
        JSONArray colorArray = (JSONArray) jsonObject.get("colorVec");
        int R = ((Long) colorArray.get(0)).intValue();
        int G = ((Long) colorArray.get(1)).intValue();
        int B = ((Long) colorArray.get(2)).intValue();
        Color color = new Color(R,G,B);
        int posX = Integer.parseInt(jsonObject.get("posX").toString());
        int posY = Integer.parseInt(jsonObject.get("posY").toString());
        String str = (String) jsonObject.get("str");

        Graphics2D g2Temp = (Graphics2D) image.getGraphics();
        g2Temp.setColor(color);
        g2Temp.drawString(str, posX, posY);
        repaint();
    }

    // LOAD drawing. the input records are list. can be generated by FileReader.
    public void loadDrawing(List<String> records) throws ParseException, IOException {
        JSONParser parser = new JSONParser();
        for (int i=0; i<records.size(); i++){
            JSONObject command = (JSONObject) parser.parse(records.get(i));
            String msgName = (String) command.get("MsgName");
            if (Objects.equals(msgName, "SendShape")){
                String drawTypeStr = (String) command.get("drawType");
                JSONArray colorArray = (JSONArray) command.get("colorVec");
                int R = ((Long) colorArray.get(0)).intValue();
                int G = ((Long) colorArray.get(1)).intValue();
                int B = ((Long) colorArray.get(2)).intValue();
                Color color = new Color(R,G,B);
                JSONArray shapeVec = (JSONArray) command.get("shapeVec");

                Vector<Position> vecP = new Vector<Position>();
                for (int j=0; j<shapeVec.size(); j++){
                    String str = (String) shapeVec.get(j);
                    JSONObject jsPos = (JSONObject) parser.parse(str);
                    Position pos = new Position(((Long)jsPos.get("x")).intValue(), ((Long)jsPos.get("y")).intValue());
                    vecP.add(pos);
                }
                DrawType localDrawType = DrawType.Line;

                switch (drawTypeStr){
                    case "\"Line\"":
                        localDrawType = DrawType.Line;
                        break;
                    case "\"Circle\"":
                        localDrawType = DrawType.Circle;
                        break;
                    case "\"Rectangle\"":
                        localDrawType = DrawType.Rectangle;
                        break;
                    case "\"Triangle\"":
                        localDrawType = DrawType.Triangle;
                        break;
                    case "\"Text\"":
                        localDrawType = DrawType.Text;
                        break;
                }
                ShapeSender shapeSender = new ShapeSender(vecP, color, localDrawType, userList, pool);
                shapeSender.sendShape();
            }
            else if (Objects.equals(msgName, "SendText")) {
                JSONArray colorArray = (JSONArray) command.get("colorVec");
                int R = ((Long) colorArray.get(0)).intValue();
                int G = ((Long) colorArray.get(1)).intValue();
                int B = ((Long) colorArray.get(2)).intValue();
                Color color = new Color(R,G,B);
                int posX = Integer.parseInt(command.get("posX").toString());
                int posY = Integer.parseInt(command.get("posY").toString());
                String str = (String) command.get("str");
                TextSender textSender = new TextSender(posX, posY, str, color, userList, pool);
                textSender.sendText();
            }
        }
    }

}