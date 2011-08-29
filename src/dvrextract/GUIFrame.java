/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dvrextract;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import javax.swing.JFrame;

/**
 *
 * @author lex
 */
public class GUIFrame extends JFrame {
    
    public GUIFrame() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(300, 300));
        setBackground(GUI.bgPanel);
        setForeground(GUI.bgPanel);
    }
    
    public void center() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(new Point(
                (screenSize.width - getWidth()) / 2,
                (screenSize.height - getHeight()) / 2));
    }
}
