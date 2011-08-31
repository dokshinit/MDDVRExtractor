package dvrextract.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import javax.swing.JFrame;

/**
 * Фрейм приложения (общие настройки и методы).
 * @author lex
 */
public class GUIFrame extends JFrame {

    /**
     * Конструктор.
     */
    public GUIFrame() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        setBackground(GUI.bgPanel);
        setForeground(GUI.bgPanel);
    }
    
    /**
     * Располагает фрейм по центру экрана.
     */
    public void center() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(new Point(
                (screenSize.width - getWidth()) / 2,
                (screenSize.height - getHeight()) / 2));
    }
}
