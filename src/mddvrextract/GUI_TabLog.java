/*
 * Copyright (c) 2011-2013, Aleksey Nikolaevich Dokshin. All right reserved.
 * Contacts: dant.it@gmail.com, dokshin@list.ru.
 */
package mddvrextract;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JPanel;

/**
 * Вкладка "Лог".
 *
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
        removeAll();

        setLayout(new BorderLayout());
        // Панель лога.
        logPanel.setMinimumSize(new Dimension(300, 300));
        add(logPanel, BorderLayout.CENTER);
    }

    /**
     * Актуализация контента при смене языка отображения.
     */
    public void updateLocale() {
        logPanel.updateLocale();
    }

    /**
     * Возвращает указатель на панель лога.
     *
     * @return Панель лога.
     */
    public GUILogPanel getLogPanel() {
        return logPanel;
    }
}
