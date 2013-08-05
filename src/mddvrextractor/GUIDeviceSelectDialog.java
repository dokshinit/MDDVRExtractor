/*
 * Copyright (c) 2011-2013, Aleksey Nikolaevich Dokshin. All right reserved.
 * Contacts: dant.it@gmail.com, dokshin@list.ru.
 */
package mddvrextractor;

import mddvrextractor.gui.GUI;
import mddvrextractor.gui.GUIDialog;
import mddvrextractor.gui.JExtComboBox;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JLabel;
import net.miginfocom.swing.MigLayout;
import mddvrextractor.xfsengine.Device;

/**
 * Диалог выбора устройства-источника (для чтения данных).
 *
 * @author Докшин Алексей Николаевич <dant.it@gmail.com>
 */
public class GUIDeviceSelectDialog extends GUIDialog implements ActionListener {

    /**
     * Список устройств.
     */
    protected JExtComboBox comboDev;
    /**
     * Кнопка подтверждения выбора.
     */
    protected JButton buttonSelect;
    /**
     * Кнопка отмены выбора.
     */
    protected JButton buttonCancel;
    /**
     * Текстовые ресурсы для интерфейса.
     */
    public static String x_Title, x_Dev, x_Select, x_Cancel, x_NoteLinux, x_NoteWindows;

    /**
     * Конструктор.
     *
     * @param owner Окно - владелец. Если null - без блокировки.
     */
    public GUIDeviceSelectDialog(Window owner) {
        super(owner);

        ArrayList<String> list = Device.list();
        for (int i = 0; i < list.size(); i++) {
        }

        setTitle(x_Title);
        setLayout(new MigLayout("fill"));

        // Источник:
        add(GUI.createLabel(x_Dev));
        // Выбор камеры:
        add(comboDev = GUI.createCombo(), "w 100:100:200, growx, wrap");
        for (int i = 0; i < list.size(); i++) {
            comboDev.addItem(i + 1, list.get(i));
        }
        comboDev.showData();
        comboDev.addActionListener(GUIDeviceSelectDialog.this);
        // Примечание:
        JLabel l = GUI.createNoteLabel(App.isWindows ? x_NoteWindows : x_NoteLinux);
        add(l, "span, growx, center, wrap");

        add(buttonSelect = GUI.createButton(x_Select), "gapy 15, h 30, sg bbb, span, split 2, center");
        buttonSelect.addActionListener(GUIDeviceSelectDialog.this);

        add(buttonCancel = GUI.createButton(x_Cancel), "sg bbb");
        buttonCancel.addActionListener(GUIDeviceSelectDialog.this);

        pack();
        setResizable(false); // После вычисления размера - изменение ни к чему.

        fireChange();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == comboDev) {
            // При выборе устройства.
            fireChange();
        } else if (e.getSource() == buttonSelect) {
            // Выбор устройства.
            fireSelect();
        } else if (e.getSource() == buttonCancel) {
            // Выход
            fireCancel();
        }
    }

    /**
     * Срабатывает при выборе устройства из списка.
     */
    public void fireChange() {
        buttonSelect.setEnabled(comboDev.getSelectedItem() != null);
    }

    /**
     * Срабатывает при нажатии кнопки подтверждения выбора.
     */
    public void fireSelect() {
        dispose();
    }

    /**
     * Срабатывает при нажатии кнопки отмены выбора.
     */
    public void fireCancel() {
        dispose();
    }
}
