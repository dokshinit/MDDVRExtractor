package dvrextract;

import dvrextract.gui.GUI;
import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;

/**
 * Вкладка "О программе".
 * @author lex
 */
public final class GUI_TabAbout extends JPanel {

    /**
     * Конструктор.
     */
    public GUI_TabAbout() {
        init();
    }

    String text1 = "<html>"
            + "<font style='font-size: 18pt; font-weight: bold'>О программе</font>"
            + "</html>";
    
    String text2 = "<html>"
            + "<font style='font-size: 12pt'>О программе</font>"
            + "<p>Автор: Докшин Алексей"
            + "<p>Версия: 0.9b<p>"
            + "</html>";
    
    /**
     * Инициализация графических компонент.
     */
    private void init() {
        setBackground(Color.GRAY);
        setLayout(new MigLayout("ins 20, debug", "grow"));
        JLabel l;
        add(GUI.createLabel(text1), "center, wrap");
        add(GUI.createLabel(text2), "center, wrap");
    }
}
