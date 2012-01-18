/*
 * Copyright (c) 2011-2012, Aleksey Nikolaevich Dokshin. All right reserved.
 * Contacts: dant.it@gmail.com, dokshin@list.ru.
 */
package dvrextract;

import dvrextract.gui.GUI;
import dvrextract.gui.GUIDialog;
import dvrextract.gui.JExtComboBox;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JLabel;
import net.miginfocom.swing.MigLayout;
import xfsengine.Device;

/**
 * Диалог выбора устройства-источника (для чтения данных).
 * @author Докшин Алексей Николаевич <dant.it@gmail.com>
 */
public class GUIDeviceSelectDialog extends GUIDialog implements ActionListener {

    protected JExtComboBox comboDev;
    protected JButton buttonSelect;
    protected JButton buttonCancel;
    /**
     * Текстовые ресурсы для интерфейса.
     */
    public static String x_Title, x_Dev, x_Select, x_Cancel, x_Hint;

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
        add(comboDev = GUI.createCombo(), "w 200, growx, wrap");
        for (int i = 0; i < list.size(); i++) {
            comboDev.addItem(i + 1, list.get(i));
        }
        comboDev.showData();
        comboDev.addActionListener(GUIDeviceSelectDialog.this);
        // Примечание:
        JLabel l = GUI.createNoteLabel(x_Hint);
        add(l, "span, growx, center, wrap");

        add(buttonSelect = GUI.createButton(x_Select), "gapy 15, h 30, span, split 2, center");
        buttonSelect.addActionListener(GUIDeviceSelectDialog.this);

        add(buttonCancel = GUI.createButton(x_Cancel));
        buttonCancel.addActionListener(GUIDeviceSelectDialog.this);

        pack();
        setResizable(false); // После вычисления размера - изменение ни к чему.

        fireChange();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == comboDev) {
            // При выборе камеры что-то делаем? Вроде нет...
            fireChange();
        } else if (e.getSource() == buttonSelect) {
            // Выбор устройства.
            fireSelect();
        } else if (e.getSource() == buttonCancel) {
            // Выход
            fireCancel();
        }
    }

    public void fireChange() {
        buttonSelect.setEnabled(comboDev.getSelectedItem() != null);
    }

    public void fireSelect() {
        dispose();
    }

    public void fireCancel() {
        dispose();
    }
}
