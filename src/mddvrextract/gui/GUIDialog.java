/*
 * Copyright (c) 2011-2013, Aleksey Nikolaevich Dokshin. All right reserved.
 * Contacts: dant.it@gmail.com, dokshin@list.ru.
 */
package mddvrextract.gui;

import java.awt.BorderLayout;
import java.awt.Window;
import javax.swing.JDialog;

/**
 * Диалог приложения (общие настройки и методы).
 *
 * @author Докшин Алексей Николаевич <dant.it@gmail.com>
 */
public class GUIDialog extends JDialog {

    /**
     * Конструктор.
     */
    public GUIDialog() {
        this(null);
    }

    /**
     * Конструктор.
     *
     * @param owner Владелец.
     */
    public GUIDialog(Window owner) {
        super(owner);
        setModal(true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
    }
}
