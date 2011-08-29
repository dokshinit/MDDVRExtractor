/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dvrextract;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author lex
 */
public final class GUI_SourceSelect extends GUIDialog implements ActionListener {

    JTextField textType;
    JExtComboBox comboCam;
    JButton buttonScan;

    public GUI_SourceSelect() {
        init();
    }

    public void init() {
        setTitle("Сканирование источника");
        setLayout(new MigLayout("fill"));

        // Тип источника:
        add(GUI.createLabel("Тип:"), "");
        add(textType = GUI.createText("не определён", 10), "");
        textType.setEditable(false);
        textType.setHorizontalAlignment(JTextField.CENTER);
        // Выбор камеры:
        add(GUI.createLabel("Камера:"), "");
        add(comboCam = GUI.createCombo(true), "push");
        comboCam.addItem(0, "< все >");
        for (int i = 0; i < App.MAXCAMS; i++) {
            comboCam.addItem(i + 1, "CAM" + (i + 1));
        }
        comboCam.showData();
        comboCam.addActionListener(this);
        // Кнопка начала сканиования:
        add(buttonScan = GUI.createButton("Сканировать"),"wrap");
        buttonScan.addActionListener(this);
        // Примечание:
        JLabel l = GUI.createNoteLabel("<html>* Ограничение по камере может существенно уменьшить время<br>сканирования HDD-источника при больших объёмах данных!</html>");
        add(l, "span, growx");
        
        pack();
        setResizable(false); // После вычисления размера - изменение ни к чему.
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == comboCam) {
            // При выборе камеры что-то делаем? Вроде нет...
        } else if (e.getSource() == buttonScan) {
            // Старт сканирования...
            App.scanTask(src, cam);
            setVisible(false);
        }
    }
}
