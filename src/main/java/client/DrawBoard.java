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

    private Vector<Position> freeVec = new Vector<Position>();
    private Vector<Position> shapeVec = new Vector<Position>(2);


    public DrawBoard(ExecutorService pool, LinkedBlockingDeque<ID> userList) {
        this.pool = pool;
        this.userList = userList;
//        image = createImage(getSize().width, getSize().height);
//        graphics2D = (Graphics2D) image.getGraphics();
//        this.image = image;
//        this.graphics2D = graphics2D;

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
//                else if (drawType == DrawType.Line){
//                    graphics2D.setColor(Color.white);
//                    graphics2D.drawLine(posStart.getX(), posStart.getY(), posEnd.getX(), posEnd.getY());
//                    graphics2D.setColor(drawColor);
//                    graphics2D.drawLine(posStart.getX(), posStart.getY(), e.getX(), e.getY());
//                    posEnd.setXY(e.getX(), e.getY());
//                    repaint();
//                }

            }
        });
        addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                if (drawType == DrawType.HandFree) {
                    posCur.setXY(e.getX(), e.getY());
                    freeVec.add(new Position(posCur));
                    // for (int i = 0; i< freeVec.size(); i++) System.out.println("In freeVec: "+ freeVec.elementAt(i).getX()+" "+ freeVec.elementAt(i).getY());
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

                    // for (int i = 0; i< shapeVec.size(); i++) System.out.println("In shapeVec: "+ shapeVec.elementAt(i).getX()+" "+ shapeVec.elementAt(i).getY());
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
            ////////////////////////
            image = createImage(getSize().width, getSize().height);
            graphics2D = (Graphics2D) image.getGraphics();
            System.out.println("Init type: "+drawType.toString());
            // clear();
        }
        graphics.drawImage(image, 0, 0, null);
    }

//    private void clear() {
//        graphics2D.setPaint(Color.white);
//        graphics2D.fillRect(0, 0, getSize().width, getSize().height);
//        repaint();
//        graphics2D.setPaint(Color.black);
//        System.out.println("clear board");
//    }

    public void setType(DrawType drawType){
        this.drawType = drawType;
        System.out.println("Draw type is set to be "+drawType.toString());
        // posText.setXY(-1,-1);
        shapeVec.clear();
    }

    public void setColor(Color color){
        this.drawColor = color;
        graphics2D.setColor(drawColor);
        System.out.println("Draw color is set to be "+drawColor.getRed()+" "+drawColor.getGreen()+" "+drawColor.getBlue());
    }


    public void receiveShape(JSONObject jsonObject) throws ParseException {
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

        System.out.println("drawTypeStr: "+drawTypeStr+" color: "+R+" "+G+" "+B+" pos vec: "
                +vecP.get(0).getX()+" "+vecP.get(0).getY()+" "+vecP.get(1).getX()+" "+vecP.get(1).getY());

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
        JSONArray colorArray = (JSONArray) jsonObject.get("colorVec");
        int R = ((Long) colorArray.get(0)).intValue();
        int G = ((Long) colorArray.get(1)).intValue();
        int B = ((Long) colorArray.get(2)).intValue();
        Color color = new Color(R,G,B);
        int posX = Integer.parseInt(jsonObject.get("posX").toString());
        int posY = Integer.parseInt(jsonObject.get("posY").toString());
        String str = (String) jsonObject.get("str");
        // System.out.println("receiveText: "+posX+" "+posY+" "+R+" "+G+" "+B+" "+str);

        Graphics2D g2Temp = (Graphics2D) image.getGraphics();
        g2Temp.setColor(color);
        g2Temp.drawString(str, posX, posY);
        repaint();
    }


}