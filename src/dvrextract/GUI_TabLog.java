/*
 * Copyright (c) 2011, Aleksey Nikolaevich Dokshin. All right reserved.
 * Contacts: dant.it@gmail.com, dokshin@list.ru.
 */
package dvrextract;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JPanel;

/**
 * Вкладка "Лог".
 * @author Докшин Алексей Николаевич <dant.it@gmail.com>
 */
public final class GUI_TabLog extends JPanel {

    /**
     * Панель лога.
     */
    private GUILogPanel logPanel;

    /**
     * Конструктор.
     */
    public GUI_TabLog() {
        logPanel = new GUILogPanel();
    }

    /**
     * Инициализация графических компонент.
     */
    public void createUI() {
        setLayout(new BorderLayout());
        // Панель лога.
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
