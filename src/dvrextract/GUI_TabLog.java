package dvrextract;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JPanel;

/**
 * Вкладка "Лог".
 * @author lex
 */
public final class GUI_TabLog extends JPanel {

    // Панель лога.
    private GUILogPanel logPanel;

    /**
     * Конструктор.
     */
    public GUI_TabLog() {
        init();
    }

    /**
     * Инициализация графических компонент.
     */
    private void init() {
        setLayout(new BorderLayout());
        // Панель лога.
        logPanel = new GUILogPanel();
        logPanel.setMinimumSize(new Dimension(300, 300));
        add(logPanel, BorderLayout.CENTER);
    }

    /**
     * Возвращает указатель на панель лога.
     * @return Панель лога.
     */
    public GUILogPanel getLogPanel() {
	return logPanel;
    }
}
