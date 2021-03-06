/*
 * Copyright (c) 2011-2013, Aleksey Nikolaevich Dokshin. All right reserved.
 * Contacts: dant.it@gmail.com, dokshin@list.ru.
 */
package mddvrextractor;

import mddvrextractor.gui.GUI;
import mddvrextractor.gui.GUIDialog;
import mddvrextractor.gui.JExtComboBox;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import net.miginfocom.swing.MigLayout;

/**
 * Диалог выбора источника (с запуском сканирования при выборе).
 *
 * @author Докшин Алексей Николаевич <dant.it@gmail.com>
 */
public final class GUI_SourceSelect extends GUIDialog implements ActionListener {

    /**
     * Отображение пути и имени файла.
     */
    private JTextField textSource;
    /**
     * Кнопка выбора источника.
     */
    private JButton buttonSelect;
    /**
     * Кнопка выбора источника-устройства.
     */
    private JButton buttonSelectDev;
    /**
     * Кнопка отмены и выхода.
     */
    private JButton buttonCancel;
    /**
     * Отображение типа источника.
     */
    private JTextField textType;
    /**
     * Отображение списка камер для ограничения сканирования.
     */
    private JExtComboBox comboCam;
    /**
     * Кнопка сканирования источника - подтверждение выбора.
     */
    private JButton buttonScan;
    /**
     * Тип выбранной ФС.
     */
    private int fstype;
    /**
     * Текстовые ресурсы для интерфейса.
     */
    public static String x_All, x_Cam, x_GoScan, x_Hint, x_Select, x_Source,
            x_Title, x_Type, x_SelectSource, x_Cancel, x_SelectDev;

    /**
     * Конструктор.
     */
    public GUI_SourceSelect() {
        super(App.gui);
        createUI();
    }

    /**
     * Инициализация графических компонетов.
     */
    private void createUI() {
        setTitle(x_Title);
        setLayout(new MigLayout("fill"));

        // Источник:
        add(GUI.createLabel(x_Source), "right");
        add(textSource = GUI.createText(30), "span, growx, split 3");
        textSource.setText(App.Source.getName().name);
        textSource.setEditable(false);
        add(buttonSelect = GUI.createButton(x_Select), "");
        buttonSelect.addActionListener(this);
        add(buttonSelectDev = GUI.createButton(x_SelectDev), "wrap");
        buttonSelectDev.addActionListener(this);
        // Тип источника:
        add(GUI.createLabel(x_Type), "right");
        add(textType = GUI.createText(App.Source.getType().title, 10), "");
        textType.setEditable(false);
        textType.setHorizontalAlignment(JTextField.CENTER);
        // Примечание:
        JLabel l = GUI.createNoteLabel(x_Hint);
        add(l, "spany 2, growx, wrap");
        // Выбор камеры:
        add(GUI.createLabel(x_Cam), "right");
        add(comboCam = GUI.createCombo(), "growx, wrap");
        comboCam.addItem(0, x_All);
        for (int i = 0; i < App.MAXCAMS; i++) {
            comboCam.addItem(i + 1, "CAM" + (i + 1));
        }
        comboCam.showData();
        comboCam.addActionListener(this);
        // Кнопка начала сканиования:
        add(buttonScan = GUI.createButton(x_GoScan), "gapy 15, h 30, sg bbb, spanx, center, split 2");
        buttonScan.addActionListener(this);
        buttonScan.setEnabled(!textSource.getText().isEmpty());
        add(buttonCancel = GUI.createButton(x_Cancel), "sg bbb, center, wrap");
        buttonCancel.addActionListener(this);

        pack();
        setResizable(false); // После вычисления размера - изменение ни к чему.

        textType.setText(SourceFileFilter.getType(App.Source.getName()).title);
        fstype = App.Source.getName().type;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == comboCam) {
            // При выборе камеры что-то делаем? Вроде нет...
        } else if (e.getSource() == buttonSelect) {
            // Выбор каталога или файла.
            fireSelect();
        } else if (e.getSource() == buttonSelectDev) {
            // Выбор устройства.
            fireSelectDev();
        } else if (e.getSource() == buttonScan) {
            // Старт сканирования...
            fireScan();
        } else if (e.getSource() == buttonCancel) {
            // Выход
            fireCancel();
        }
    }

    /**
     * Обработка нажатия на кнопку выбора источника. Вызывается диалог выбора
     * файла/каталога.
     */
    private void fireSelect() {
        SelectDialog dlg = new SelectDialog();
        GUI.centerizeFrame(dlg, App.gui);
        dlg.setVisible(true);
    }

    /**
     * Обработка нажатия на кнопку выбора устройства-источника. Вызывается
     * диалог выбора устройства.
     */
    private void fireSelectDev() {
        SelectDevDialog dlg = new SelectDevDialog();
        GUI.centerizeFrame(dlg, App.gui);
        dlg.setVisible(true);
    }

    /**
     * Обработка нажатия на кнопку сканирования. Стартует сканирование
     * источника. Можно запускать только при отсутсвии текущего процесса
     * сканирования \ обработки.
     */
    private void fireScan() {
        // Запуск задачи сканирования.
        if (Task.start(new ScanTask())) {
            dispose(); // В случас успеха - закрываем окно.
        }
    }

    /**
     * Обработка нажатия на кнопку сканирования. Стартует сканирование
     * источника. Можно запускать только при отсутсвии текущего процесса
     * сканирования \ обработки.
     */
    private void fireCancel() {
        dispose(); // Закрываем окно.
    }

    /**
     * Задача сканирования источника.
     */
    private class ScanTask extends Task.Thread {

        @Override
        public void task() {
            // Сканирование источника.
            Files.scan(new FileDesc(textSource.getText(), fstype), comboCam.getSelectedItem().id);
        }
    }

    /**
     * Диалог выбора существующего файла/каталога источника.
     */
    private class SelectDialog extends GUIFileSelectDialog {

        /**
         * Конструктор.
         */
        private SelectDialog() {
            super(GUI_SourceSelect.this, x_SelectSource,
                    textSource.getText().trim(), "", Target.EXIST_ONLY, Mode.ALL);
        }

        @Override
        public void fireInit(FileChooser fc) {
            fc.setAcceptAllFileFilterUsed(false);
            fc.addChoosableFileFilter(SourceFileFilter.instALL);
            fc.addChoosableFileFilter(SourceFileFilter.instEXE);
            fc.addChoosableFileFilter(SourceFileFilter.instHDD);
            fc.setFileFilter(SourceFileFilter.get(new FileDesc(textSource.getText())));
        }

        @Override
        public void fireApply(FileChooser fc) throws CancelActionExeption {
            final File f = fc.getSelectedFile();
            textSource.setText(f.getAbsolutePath());
            textType.setText(SourceFileFilter.getType(new FileDesc(f.getName())).title);
            buttonScan.setEnabled(!textSource.getText().isEmpty());
            fstype = FileDesc.FS;
        }
    }

    /**
     * Диалог выбора устройства-источника.
     */
    private class SelectDevDialog extends GUIDeviceSelectDialog {

        /**
         * Конструктор.
         */
        private SelectDevDialog() {
            super(GUI_SourceSelect.this);
        }

        @Override
        public void fireSelect() {
            String dev = (String) comboDev.getSelectedItem().object;
            textSource.setText(dev);
            textType.setText(FileType.XFS.title);
            buttonScan.setEnabled(!textSource.getText().isEmpty());
            fstype = FileDesc.XFS;
            super.fireSelect();
        }
    }
}
