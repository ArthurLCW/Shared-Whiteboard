package client;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import util.*;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Objects;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

public class WhiteboardFrame extends JFrame {
    private JPanel WhiteboardPanel;
    private JPanel LeftPanel;
    private JPanel RightPanel;
    private JRadioButton FreeHandBtn;
    private JLabel DrawChoiceLabel;
    private JRadioButton TextBtn;
    private JRadioButton ShapeBtn;
    private JRadioButton testBtn;
    private JComboBox ShapeCombo;
    private JPanel BottomPanel;
    private JPanel BottomSubPanel0;
    private JPanel BottomSubPanel1;
    private JLabel ColorChoiceLabel;
    private JComboBox ColorCombo;
    private JPanel DrawPanel;
    private JButton CustomizedColorButton;
    private JButton button1;

    private DrawType drawType;
    private static DrawBoard drawBoard;

    // color chosen board
    private JPanel colorChosenBackPanel;
    private JOptionPane colorChosenPanel;
    private JTextField inputR, inputG, inputB;
    private JLabel colorChooseInfo;

    private Image image;
    private static Graphics2D graphics2D;

    private static ExecutorService pool;
    static final int MAX_T = 8;
    private static LinkedBlockingDeque<Socket> socketQueue;
    private static LinkedBlockingDeque<ID> userList;


    // user info
    private static int portServer = 3200;
    private static int portMy = 3201;
    private static String username = "admin";
    private static InetAddress serverIP;
    private static int timeout = 1000;
    private static JFrame frame;
    private static ID managerInfo;

    private JPanel mainPanel;
    private JList userJList;
    private JList chatRoomJList;
    private JTextArea MsgInput;
    private JLabel UserListLabel;
    private JLabel ChatRoomLabel;
    private JButton SendMsgButton;
    private JPanel FilePanel;
    private JButton NewBtn;
    private JButton SaveAsBtn;
    private JButton OpenBtn;
    private JButton SaveBtn;
    private JButton KickBtn;
    private JScrollPane UserListPane;
    private JScrollPane ChatRoomPane;
    static private DefaultListModel userlistModel;
    private static DefaultListModel chatRoomListModel;

    static {
        try {
            managerInfo = new ID("", InetAddress.getByName("localhost"), -1);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
    private static ManagerFlag managerFlag;

    public WhiteboardFrame(String appName) throws UnknownHostException {
        super(appName);
        setupUI();
        // $$$setupUI$$$();
        this.frame = this;
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(WhiteboardPanel);
        this.pack();
        initWhiteBoard();

        this.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                System.out.println("Closed!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                Socket leaveSocket = null;
                try {
                    leaveSocket = new Socket(InetAddress.getByName(serverIP.getHostName()), portServer);
                    LeaveRequestSender leaveRequestSender = new LeaveRequestSender(leaveSocket, username, portMy);
                    LeaveRequestSender.send();
                    System.out.println("manager?"+managerInfo.getUsername());
//                    RecordSaver recordSaver = new RecordSaver("C:\\Users\\Arthu\\Desktop\\0.whiteboard", drawBoard.drawingRecord);
//                    int status = recordSaver.saveFile();

                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }


                e.getWindow().dispose();
            }
        });
        SendMsgButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                System.out.println("Clicked!!!!!!!!!!!!!!!!!");
                String msg = MsgInput.getText();
                if (msg.length()==0){
                    JOptionPane.showMessageDialog(frame,
                            "You have not typed anything. ");
                }
                else{
                    ChatMsgSender chatMsgSender = new ChatMsgSender(userList, pool, username, msg);
                    chatMsgSender.sendChatMsg();
                    MsgInput.setText("");
                }
            }
        });
        KickBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                boolean iAmManager = managerFlag.getFlag();

                if (iAmManager){
                    int selectedIndex = -1;
                    try{
                        selectedIndex = userJList.getSelectedIndex();
                    }finally {
                        if (selectedIndex==-1){
                            JOptionPane.showMessageDialog(frame, "You have not chosen a user. ");
                        }else{
                            if (selectedIndex==0){
                                JOptionPane.showMessageDialog(frame, "You cannot kick yourself. ");
                            }else{
                                int decision = JOptionPane.showOptionDialog(frame,
                                        "Are you sure you want to kick "+(String) userJList.getSelectedValue(),
                                        "Kick Confirmation",
                                        JOptionPane.YES_NO_OPTION,
                                        JOptionPane.QUESTION_MESSAGE,
                                        null,null,null); //default button title
                                if (decision == JOptionPane.YES_OPTION){
                                    ID id = null;
                                    Iterator iteratorVals = userList.iterator();
                                    for (int i=0; i<=selectedIndex; i++){
                                        iteratorVals.hasNext();
                                        id = (ID) iteratorVals.next();
                                    }
                                    try {
                                        JSONObject jsonObject = new JSONObject();
                                        jsonObject.put("MsgName", "KickOut");
                                        String kickMsg = jsonObject.toJSONString();
                                        Socket kickSocket = new Socket(id.getIP(), id.getPort());
                                        DataOutputStream outputK = new DataOutputStream(kickSocket.getOutputStream());
                                        outputK.writeUTF(kickMsg);
                                        kickSocket.close();

                                        jsonObject.put("username", id.getUsername());
                                        jsonObject.put("userIP", id.getIP().getHostName());
                                        jsonObject.put("userServerPort", id.getPort());
                                        String kickServerMsg = jsonObject.toJSONString();
                                        Socket kickServerSocket = new Socket(serverIP, portServer);
                                        DataOutputStream outputSK = new DataOutputStream(kickServerSocket.getOutputStream());
                                        outputSK.writeUTF(kickServerMsg);
                                        kickServerSocket.close();

                                    } catch (IOException ex) {
                                        throw new RuntimeException(ex);
                                    }
                                }
                            }
                        }
                    }
                }else{
                    JOptionPane.showMessageDialog(frame, "You are not manager. You cannot kick users.");
                }
            }
        });
        FreeHandBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                drawBoard.setType(DrawType.HandFree);
            }
        });
        TextBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                drawBoard.setType(DrawType.Text);
            }
        });
        ShapeBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                switch ((String) ShapeCombo.getSelectedItem()) {
                    case "Line":
                        drawBoard.setType(DrawType.Line);
                        break;
                    case "Circle":
                        drawBoard.setType(DrawType.Circle);
                        break;
                    case "Triangle":
                        drawBoard.setType(DrawType.Triangle);
                        break;
                    case "Rectangle":
                        drawBoard.setType(DrawType.Rectangle);
                        break;
                }
            }
        });
        ShapeCombo.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (ShapeBtn.isSelected()) {
                    switch ((String) ShapeCombo.getSelectedItem()) {
                        case "Line":
                            drawBoard.setType(DrawType.Line);
                            break;
                        case "Circle":
                            drawBoard.setType(DrawType.Circle);
                            break;
                        case "Triangle":
                            drawBoard.setType(DrawType.Triangle);
                            break;
                        case "Rectangle":
                            drawBoard.setType(DrawType.Rectangle);
                            break;
                    }
                }
            }
        });
        ColorCombo.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                drawBoard.setColor((Color) ColorCombo.getSelectedItem());
            }
        });
        CustomizedColorButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                Object[] options = {"Confirm", "Cancel"};

                int R, G, B;
                while (true) {
                    try {
                        int result = JOptionPane.showOptionDialog(null, colorChosenBackPanel,
                                "RGB Color Customization", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                                null, options, options[0]);

                        if (result == JOptionPane.OK_OPTION) {
                            System.out.println("RGB: " + inputR.getText() + " " + inputG.getText() + " " + inputB.getText());
                            R = Integer.parseInt(inputR.getText());
                            G = Integer.parseInt(inputG.getText());
                            B = Integer.parseInt(inputB.getText());
                            inputR.setText("");
                            inputG.setText("");
                            inputB.setText("");

                            if (R < 0 || R > 255 || G < 0 || G > 255 || B < 0 || B > 255) {
                                colorChooseInfo.setText("Your inputs are incorrect. Please try again. The value should be integer from 0-255");
                            } else {
                                Color newColor = new Color(R, G, B);
                                if (checkColorIndex(newColor) < 0) {
                                    ColorCombo.addItem(newColor);
                                    ColorCombo.setSelectedIndex(ColorCombo.getItemCount() - 1);
                                } else {
                                    ColorCombo.setSelectedIndex(checkColorIndex(newColor));
                                }
                                break;
                            }
                        } else {
                            break;
                        }

                    } catch (NumberFormatException intFormatE) {
                        colorChooseInfo.setText("Your inputs are incorrect. Please try again. The value should be integer from 0-255");
                    }
                }
            }
        });
    }

    private void createUIComponents() {
        userList = new LinkedBlockingDeque<ID>();
        pool = Executors.newFixedThreadPool(MAX_T);

        drawBoard = new DrawBoard(pool, userList);
        DrawPanel = drawBoard;
        initRightPanel();
    }

    public void initRightPanel() {
        userlistModel = new DefaultListModel();
        userJList = new JList(userlistModel);
        UserListPane = new JScrollPane(userJList);

        chatRoomListModel = new DefaultListModel();
        chatRoomJList = new JList(chatRoomListModel);
        ChatRoomPane = new JScrollPane(chatRoomJList);
    }

    private void initColorChosenBoard() {
        JLabel chooseRLabel = new JLabel("Please input R value: ");
        JLabel chooseGLabel = new JLabel("Please input G value: ");
        JLabel chooseBLabel = new JLabel("Please input B value: ");
        inputR = new JTextField(3);
        inputG = new JTextField(3);
        inputB = new JTextField(3);
        colorChosenBackPanel = new JPanel(new GridLayout(2, 1));
        colorChooseInfo = new JLabel();
        colorChooseInfo.setText("Please enter integer from 0-255. ");
        JPanel colorChosenBackPanel0 = new JPanel();
        JPanel colorChosenBackPanel1 = new JPanel();
        colorChosenBackPanel0.add(colorChooseInfo);

        colorChosenBackPanel1.add(chooseRLabel);
        colorChosenBackPanel1.add(inputR);
        colorChosenBackPanel1.add(new JLabel(";       "));

        colorChosenBackPanel1.add(chooseGLabel);
        colorChosenBackPanel1.add(inputG);
        colorChosenBackPanel1.add(new JLabel(";       "));

        colorChosenBackPanel1.add(chooseBLabel);
        colorChosenBackPanel1.add(inputB);
        colorChosenBackPanel1.add(new JLabel("."));

        colorChosenBackPanel.add(colorChosenBackPanel0, 0, 0);
        colorChosenBackPanel.add(colorChosenBackPanel1, 1, 1);
    }

    public DefaultListModel getUserlistModel() {return userlistModel;}
    public DefaultListModel getchatRoomListModel() {return chatRoomListModel;}

    int checkColorIndex(Color color) {
        int idx = -1;
        for (int i = 0; i < ColorCombo.getItemCount(); i++) {
            if (color.equals(ColorCombo.getItemAt(i))) {
                idx = i;
                break;
            }
        }
        return idx;
    }

    public static void main(String[] args) throws IOException, ParseException, InterruptedException {
        SimpleDateFormat ft = new SimpleDateFormat("hh:mm:ss");
        WhiteboardFrame whiteboardFrame = new WhiteboardFrame("Shared Whiteboard");
        whiteboardFrame.setVisible(true);
        System.out.println("Main len: " + args.length);
        try {
            serverIP = InetAddress.getByName("localhost");
            username = args[0];
            portServer = Integer.parseInt(args[1]);
            serverIP = InetAddress.getByName(args[2]);
            portMy = Integer.parseInt(args[3]);
            System.out.println("client connect: Username: " + username + " portServer: " + portServer +
                    " serverIP: " + serverIP + " portMy: " + portMy);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println(e.toString());
        }
        managerFlag = new ManagerFlag();


        // TODO: WXH: an example of how to save files, load files.
        // not sure if loaded drawing needs to be seen by others too.....
//        RecordSaver recordSaver = new RecordSaver("C:\\Users\\Arthu\\Desktop\\0.whiteboard", drawBoard.drawingRecord);
//        int status = recordSaver.saveFile();

        // file reader
//        RecordReader fileReader = new RecordReader("C:\\Users\\Arthu\\Desktop\\0.whiteboard");
//        fileReader.readFile();
//        drawBoard.loadDrawing(fileReader.getRecords());
//        System.out.println("Records size: "+drawBoard.drawingRecord.size());



        // initial socket Connecting, ask for join in permission
//        System.out.println(InetAddress.getByName(serverIP.getHostName()));
        Socket joinInSocket = new Socket(InetAddress.getByName(serverIP.getHostName()), portServer);
        JoinInRequestSender joinInRequestSender = new JoinInRequestSender(joinInSocket, username, portMy);
        joinInRequestSender.send();

        // receive the full current ip/port/name tuples
        // TODO: wxh may need to implement "deny" function (socket and ui).



        socketQueue = new LinkedBlockingDeque<Socket>();
//        userList = new LinkedBlockingDeque<ID>();
//        LinkedBlockingDeque<ID> xx = new LinkedBlockingDeque<ID>();

        IOThread ioThread = new IOThread(portMy, socketQueue, timeout); // used to receive all sockets and store in a queue
        ioThread.start();
        WorkThread workThread = new WorkThread(pool, socketQueue, userList, drawBoard, frame, managerInfo, serverIP,
                portServer, drawBoard.drawingRecord, userlistModel,managerFlag, chatRoomListModel);
        workThread.start();


    }

    private void initWhiteBoard() {
        ButtonGroup choiceGroup = new ButtonGroup();
        choiceGroup.add(FreeHandBtn);
        choiceGroup.add(TextBtn);
        choiceGroup.add(ShapeBtn);

        String[] shapeList = {"Line", "Circle", "Triangle", "Rectangle"};
        for (int i = 0; i < shapeList.length; i++) ShapeCombo.addItem(shapeList[i]);
        ShapeCombo.setSelectedIndex(0);

        Color[] colorList = {Color.black, Color.red, Color.blue, Color.green, Color.yellow, Color.magenta, Color.cyan,
                Color.darkGray, Color.gray, Color.lightGray, Color.orange, Color.pink,
                new Color(66, 123, 123), new Color(123, 66, 234),
                new Color(99, 88, 66), new Color(66, 234, 88), Color.white};
        for (int i = 0; i < colorList.length; i++) ColorCombo.addItem(colorList[i]);
        ColorCombo.setSelectedIndex(0);
        ColorCombo.setRenderer(new ColorRenderer());
        initColorChosenBoard();

        socketQueue = new LinkedBlockingDeque<Socket>();
    }



    class ColorRenderer extends JLabel implements ListCellRenderer {
        // color rendering inspired by https://stackoverflow.com/questions/18830098/pick-color-with-jcombobox-java-swing
        public ColorRenderer() {
            setOpaque(true);
        }

        boolean colorChanged = false;

        @Override
        public void setBackground(Color bg) {
            if (!colorChanged) return;
            super.setBackground(bg);
        }

        public Component getListCellRendererComponent(
                JList list,
                Object color,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {
            colorChanged = true;
            setText(" ");
            setBackground((Color) color);
            colorChanged = false;
            return this;
        }
    }



//    {
//// GUI initializer generated by IntelliJ IDEA GUI Designer
//// >>> IMPORTANT!! <<<
//// DO NOT EDIT OR ADD ANY CODE HERE!
//        setupUI();
//    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void setupUI() {
        createUIComponents();
        WhiteboardPanel = new JPanel();
        WhiteboardPanel.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        LeftPanel = new JPanel();
        LeftPanel.setLayout(new GridLayoutManager(2, 3, new Insets(0, 0, 0, 0), -1, -1));
        LeftPanel.setBackground(new Color(-1));
        LeftPanel.setForeground(new Color(-1));
        WhiteboardPanel.add(LeftPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(800, 700), new Dimension(800, 700), new Dimension(800, 700), 0, false));
        LeftPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        BottomPanel = new JPanel();
        BottomPanel.setLayout(new GridLayoutManager(2, 5, new Insets(0, 0, 0, 0), -1, -1));
        LeftPanel.add(BottomPanel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_SOUTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 50), new Dimension(-1, 50), new Dimension(-1, 50), 0, false));
        BottomPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        DrawChoiceLabel = new JLabel();
        DrawChoiceLabel.setText("Drawing method: ");
        BottomPanel.add(DrawChoiceLabel, new GridConstraints(0, 0, 2, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        FreeHandBtn = new JRadioButton();
        FreeHandBtn.setHideActionText(false);
        FreeHandBtn.setSelected(true);
        FreeHandBtn.setText("Free Hand");
        BottomPanel.add(FreeHandBtn, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        TextBtn = new JRadioButton();
        TextBtn.setText("Text");
        BottomPanel.add(TextBtn, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        BottomSubPanel0 = new JPanel();
        BottomSubPanel0.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        BottomPanel.add(BottomSubPanel0, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_VERTICAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        ShapeBtn = new JRadioButton();
        ShapeBtn.setText("Shape");
        BottomSubPanel0.add(ShapeBtn, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        ShapeCombo = new JComboBox();
        BottomSubPanel0.add(ShapeCombo, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        BottomSubPanel1 = new JPanel();
        BottomSubPanel1.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        BottomPanel.add(BottomSubPanel1, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        BottomSubPanel1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        ColorChoiceLabel = new JLabel();
        ColorChoiceLabel.setText("Choose color: ");
        BottomSubPanel1.add(ColorChoiceLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        ColorCombo = new JComboBox();
        BottomSubPanel1.add(ColorCombo, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(100, -1), new Dimension(100, -1), new Dimension(100, -1), 0, false));
        CustomizedColorButton = new JButton();
        CustomizedColorButton.setText("Customized Color");
        BottomSubPanel1.add(CustomizedColorButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        DrawPanel.setBackground(new Color(-1));
        DrawPanel.setForeground(new Color(-1));
        LeftPanel.add(DrawPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, new Dimension(-1, 650), new Dimension(-1, 650), new Dimension(-1, 650), 0, false));
        DrawPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));


        RightPanel = new JPanel();
        RightPanel.setLayout(new GridLayoutManager(8, 1, new Insets(0, 0, 0, 0), -1, -1));
        WhiteboardPanel.add(RightPanel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(400, 700), new Dimension(400, 700), new Dimension(400, 700), 0, false));

        RightPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));

        MsgInput = new JTextArea();
        RightPanel.add(MsgInput, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, new Dimension(400, 50), new Dimension(400, 50), new Dimension(400, 50), 0, false));
        UserListLabel = new JLabel();
        UserListLabel.setText("User List:");
        RightPanel.add(UserListLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(300, 25), new Dimension(300, 25), new Dimension(300, 25), 0, false));
        ChatRoomLabel = new JLabel();
        ChatRoomLabel.setText("Chat Room:");
        RightPanel.add(ChatRoomLabel, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(400, 25), new Dimension(400, 25), new Dimension(400, 25), 0, false));
        FilePanel = new JPanel();
        FilePanel.setLayout(new GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), -1, -1));
        RightPanel.add(FilePanel, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(400, 50), new Dimension(400, 50), new Dimension(400, 50), 0, false));
        NewBtn = new JButton();
        NewBtn.setText("New");
        FilePanel.add(NewBtn, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        SaveAsBtn = new JButton();
        SaveAsBtn.setText("Save As");
        FilePanel.add(SaveAsBtn, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        OpenBtn = new JButton();
        OpenBtn.setText("Open");
        FilePanel.add(OpenBtn, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        SaveBtn = new JButton();
        SaveBtn.setText("Save");
        FilePanel.add(SaveBtn, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        SendMsgButton = new JButton();
        SendMsgButton.setText("Send");
        RightPanel.add(SendMsgButton, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(400, 25), new Dimension(400, 25), new Dimension(400, 50), 0, false));
        KickBtn = new JButton();
        KickBtn.setText("Kick");
        RightPanel.add(KickBtn, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(400, 25), new Dimension(400, 25), new Dimension(400, 25), 0, false));

        // UserListPane = new JScrollPane(); // may need to be deleted!


        RightPanel.add(UserListPane, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(400, 225), new Dimension(400, 225), new Dimension(400, 225), 0, false));

        // ChatRoomPane = new JScrollPane(); // may need to be deleted!


        RightPanel.add(ChatRoomPane, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(400, 225), new Dimension(400, 225), new Dimension(400, 225), 0, false));

    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return WhiteboardPanel;
    }
}
