/*
 * Copyright (c) 2011-2013, Aleksey Nikolaevich Dokshin. All right reserved.
 * Contacts: dant.it@gmail.com, dokshin@list.ru.
 */
package mddvrextract.gui;

import java.awt.BorderLayout;
import javax.swing.JFrame;

/**
 * Фрейм приложения (общие настройки и методы).
 *
 * @author Докшин Алексей Николаевич <dant.it@gmail.com>
 */
public class GUIFrame extends JFrame {

    /**
     * Конструктор.
     */
    public GUIFrame() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
    }
}
