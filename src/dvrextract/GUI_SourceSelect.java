/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dvrextract;

import dvrextract.gui.GUI;
import dvrextract.gui.GUIDialog;
import dvrextract.gui.JExtComboBox;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author lex
 */
public final class GUI_SourceSelect extends GUIDialog implements ActionListener {

    JTextField textSource;
    JButton buttonSelect;
    JTextField textType;
    JExtComboBox comboCam;
    JButton buttonScan;
    int type; // тип выбранного файла: 0 - не определен, 1 - EXE, 2 - HDD.

    public GUI_SourceSelect() {
        type = 0;
        init();
    }

    public void init() {
        setTitle("Сканирование источника");
        setLayout(new MigLayout("fill"));

        // Источник:
        add(GUI.createLabel("Источник:"), "");
        add(textSource = GUI.createText(30), "span, growx, split 2");
        textSource.setText(App.srcName);
        textSource.setEditable(false);
        add(buttonSelect = GUI.createButton("Выбор"), "wrap");
        buttonSelect.addActionListener(this);
        // Тип источника:
        add(GUI.createLabel("Тип:"));
        add(textType = GUI.createText("не определён", 10));
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
        add(buttonScan = GUI.createButton("Сканировать"), "wrap");
        buttonScan.addActionListener(this);
        // Примечание:
        JLabel l = GUI.createNoteLabel("<html>* Ограничение по камере может существенно уменьшить время<br>сканирования HDD-источника при больших объёмах данных!</html>");
        add(l, "span, growx");

        pack();
        setResizable(false); // После вычисления размера - изменение ни к чему.

        File f = new File(textSource.getText());
        textType.setText(SourceFileFilter.getTypeTitle(f));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == comboCam) {
            // При выборе камеры что-то делаем? Вроде нет...
            //
        } else if (e.getSource() == buttonSelect) {
            // Выбор каталога или файла.
            do_Select();
        } else if (e.getSource() == buttonScan) {
            // Старт сканирования...
            do_Scan();
        }

    }

    private void do_Select() {
        UIManager.put("FileChooser.readOnly", true);

        UIManager.put("FileChooser.cancelButtonText", "Отмена");
        UIManager.put("FileChooser.cancelButtonToolTipText", "Отмена выбора");
        UIManager.put("FileChooser.detailsViewButtonToolTipText", "Детальный вид");
        UIManager.put("FileChooser.listViewButtonToolTipText", "В виде списка");
        UIManager.put("FileChooser.fileNameLabelText", "Имя файла:");
        UIManager.put("FileChooser.filesOfTypeLabelText", "Тип файлов:");
        UIManager.put("FileChooser.homeFolderToolTipText", "Домашний каталог");
        UIManager.put("FileChooser.lookInLabelText", "Каталог:");
        UIManager.put("FileChooser.openButtonText", "Выбрать");
        UIManager.put("FileChooser.openButtonToolTipText", "Выбрать файл");
        UIManager.put("FileChooser.upFolderToolTipText", "На уровень вверх");
        UIManager.put("FileChooser.fileDateHeaderText", "Дата/время");
        UIManager.put("FileChooser.fileNameHeaderText", "Имя");
        UIManager.put("FileChooser.fileSizeHeaderText", "Размер");
        UIManager.put("FileChooser.detailsViewActionLabelText", "Детальный");
        UIManager.put("FileChooser.listViewActionLabelText", "Списком");
        UIManager.put("FileChooser.refreshActionLabelText", "Обновить");
        UIManager.put("FileChooser.viewMenuLabelText", "Вид");

        JFileChooser fd = new JFileChooser(textSource.getText()) {

            @Override
            public void approveSelection() {
                if (getSelectedFile().exists()) {
                    super.approveSelection();
                } else {
                    JOptionPane.showMessageDialog(this, "Выбранный файл/каталог не существует!");
                }
            }
        };
        fd.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fd.setAcceptAllFileFilterUsed(false);
        fd.addChoosableFileFilter(SourceFileFilter.instEXE);
        fd.addChoosableFileFilter(SourceFileFilter.instHDD);
        fd.setFileFilter(type == 2 ? SourceFileFilter.instHDD : SourceFileFilter.instEXE);
        fd.setDialogTitle("Выбор файла/каталога источника");
        fd.setMultiSelectionEnabled(false);
        String name = textSource.getText().trim();
        if (name.length() > 0) {
            fd.setSelectedFile(new File(name));
        }

        int res = fd.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File f = fd.getSelectedFile();
            textSource.setText(f.getAbsolutePath());
            textType.setText(SourceFileFilter.getTypeTitle(type));
        }
    }

    private void do_Scan() {
        App.scanTask(textSource.getText(), type, comboCam.getSelectedItem().id);
        dispose();
    }
}