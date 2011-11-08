package dvrextract.gui;

import java.awt.BorderLayout;
import java.awt.Window;
import javax.swing.JDialog;

/**
 * Диалог приложения (общие настройки и методы).
 * @author lex
 */
public class GUIDialog extends JDialog {

    /**
     * Конструктор.
     */
    public GUIDialog() {
        this(null);
    }
    
    public GUIDialog(Window owner) {
        super(owner);
        setModal(true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        setBackground(GUI.bgPanel);
        setForeground(GUI.bgPanel);
    }
}
