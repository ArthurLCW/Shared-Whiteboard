package client;

import util.TextSender;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class RightBoard extends JPanel {

    public RightBoard(){
        this.setSize(400, 700);
        this.setBackground(new Color(34,2,44));
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                System.out.println("xxxxxxxxxxxxx"+e.getX()+" "+e.getY());
            }
        });
    }

}
