package dvrextract;

import dvrextract.gui.GUI;
import dvrextract.gui.GUIDialog;
import dvrextract.gui.JExtComboBox;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import net.miginfocom.swing.MigLayout;

/**
 * Диалог выбора источника (с запуском сканирования при выборе).
 * @author lex
 */
public final class GUI_SourceSelect extends GUIDialog implements ActionListener {

    // Отображение пути и имени файла.
    private JTextField textSource;
    // Кнопка выбора источника.
    private JButton buttonSelect;
    // Отображение типа источника.
    private JTextField textType;
    // Отображение списка камер для ограничения сканирования.
    private JExtComboBox comboCam;
    // Кнопка сканирования источника - подтверждение выбора.
    private JButton buttonScan;

    /**
     * Конструктор.
     */
    public GUI_SourceSelect() {
        super(App.mainFrame);
        init();
    }

    /**
     * Инициализация графических компонетов.
     */
    private void init() {
        setTitle("Сканирование источника");
        setLayout(new MigLayout("fill"));

        // Источник:
        add(GUI.createLabel("Источник"), "right");
        add(textSource = GUI.createText(30), "span, growx, split 2");
        textSource.setText(App.srcName);
        textSource.setEditable(false);
        add(buttonSelect = GUI.createButton("Выбор"), "wrap");
        buttonSelect.addActionListener(this);
        // Тип источника:
        add(GUI.createLabel("Тип"), "right");
        add(textType = GUI.createText("не определён", 10), "");
        textType.setEditable(false);
        textType.setHorizontalAlignment(JTextField.CENTER);
        // Примечание:
        JLabel l = GUI.createNoteLabel("Выбор конкретной камеры может существенно<br>уменьшить время сканирования источника при<br>больших объёмах данных!");
        add(l, "spany 2, wrap");
        // Выбор камеры:
        add(GUI.createLabel("Камера"), "right");
        add(comboCam = GUI.createCombo(true), "growx, wrap");
        comboCam.addItem(0, "< все >");
        for (int i = 0; i < App.MAXCAMS; i++) {
            comboCam.addItem(i + 1, "CAM" + (i + 1));
        }
        comboCam.showData();
        comboCam.addActionListener(this);
        // Кнопка начала сканиования:
        add(buttonScan = GUI.createButton("Сканировать"), "gapy 15, h 30, span, center, wrap");
        buttonScan.addActionListener(this);

        pack();
        setResizable(false); // После вычисления размера - изменение ни к чему.

        textType.setText(SourceFileFilter.getType(textSource.getText()).title);
    }

    /**
     * Обработка событий нажатий на кнопки.
     * @param e 
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == comboCam) {
            // При выборе камеры что-то делаем? Вроде нет...
        } else if (e.getSource() == buttonSelect) {
            // Выбор каталога или файла.
            fireSelect();
        } else if (e.getSource() == buttonScan) {
            // Старт сканирования...
            fireScan();
        }
    }

    /**
     * Обработка нажатия на кнопку выбора источника.
     * Вызывается диалог выбора файла/каталога.
     */
    private void fireSelect() {
        SelectDialog dlg = new SelectDialog();
        GUI.centerizeFrame(dlg, App.mainFrame);
        dlg.setVisible(true);
    }

    /**
     * Обработка нажатия на кнопку сканирования.
     * Стартует сканирование источника. Можно запускать только при отсутсвии 
     * текущего процесса сканирования \ обработки.
     */
    private void fireScan() {
        // Запуск задачи сканирования.
        if (Task.start(new ScanTask())) {
            dispose();
        }
    }

    private class ScanTask extends Task.Thread {

        @Override
        public void task() {
            // Сканирование источника.
            Files.scan(textSource.getText(), comboCam.getSelectedItem().id);
        }
    }

    /**
     * Диалог выбора существующего файла/каталога источника.
     */
    private class SelectDialog extends GUIFileSelectDialog {

        private SelectDialog() {
            super(GUI_SourceSelect.this, "Выбор файла/каталога источника",
                    textSource.getText().trim(), Target.EXIST_ONLY, Mode.ALL);
        }

        @Override
        public void fireInit(FileChooser fc) {
            fc.setAcceptAllFileFilterUsed(false);
            fc.addChoosableFileFilter(SourceFileFilter.instALL);
            fc.addChoosableFileFilter(SourceFileFilter.instEXE);
            fc.addChoosableFileFilter(SourceFileFilter.instHDD);
            fc.setFileFilter(SourceFileFilter.get(textSource.getText()));
        }

        @Override
        public void fireApply(FileChooser fc) throws CancelActionExeption {
            final File f = fc.getSelectedFile();
            textSource.setText(f.getAbsolutePath());
            textType.setText(SourceFileFilter.getType(f).title);
        }
    }
}
