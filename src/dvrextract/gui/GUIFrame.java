package dvrextract.gui;

import java.awt.BorderLayout;
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
}
