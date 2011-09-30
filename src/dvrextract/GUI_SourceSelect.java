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
    //
    // Тип выбранного файла.
    private FileType type;

    /**
     * Конструктор.
     */
    public GUI_SourceSelect() {
        type = FileType.NO;
        init();
    }

    /**
     * Инициализация графических компонетов.
     */
    private void init() {
        setTitle("Сканирование источника");
        setLayout(new MigLayout("fill"));

        // Источник:
        add(GUI.createLabel("Источник:"), "right");
        add(textSource = GUI.createText(30), "span, growx, split 2");
        textSource.setText(App.srcName);
        textSource.setEditable(false);
        add(buttonSelect = GUI.createButton("Выбор"), "wrap");
        buttonSelect.addActionListener(this);
        // Тип источника:
        add(GUI.createLabel("Тип:"), "right");
        add(textType = GUI.createText("не определён", 10), "");
        textType.setEditable(false);
        textType.setHorizontalAlignment(JTextField.CENTER);
        // Примечание:
        JLabel l = GUI.createNoteLabel("Выбор конкретной камеры может существенно<br>уменьшить время сканирования источника при<br>больших объёмах данных!");
        add(l, "spany 2, wrap");
        // Выбор камеры:
        add(GUI.createLabel("Камера:"), "right");
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

        File f = new File(textSource.getText());
        type = SourceFileFilter.getType(f);
        textType.setText(type.title);
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

        JFileChooser fd = new JFileChooser() {

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
        fd.addChoosableFileFilter(SourceFileFilter.instALL);
        fd.addChoosableFileFilter(SourceFileFilter.instEXE);
        fd.addChoosableFileFilter(SourceFileFilter.instHDD);
        fd.setFileFilter(SourceFileFilter.get(type));
        fd.setDialogTitle("Выбор файла/каталога источника");
        fd.setMultiSelectionEnabled(false);
        String name = textSource.getText().trim();
        if (name.length() > 0) {
            File f = new File(name);
            fd.setCurrentDirectory(f);
            fd.setSelectedFile(f);
        }

        int res = fd.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File f = fd.getSelectedFile();
            textSource.setText(f.getAbsolutePath());
            type = SourceFileFilter.getType(f);
            textType.setText(type.title);
        }
    }

    /**
     * Обработка нажатия на кнопку сканирования.
     * Стартует сканирование источника. Можно запускать только при отсутсвии 
     * текущего процесса сканирования \ обработки.
     */
    private void fireScan() {
        // Установка источника.
        App.srcName = textSource.getText();
        App.srcType = type;
        App.srcCamLimit = comboCam.getSelectedItem().id;
        // Очистка отображаемого списка камер источника.
        App.mainFrame.tabSource.displayCams(0);
        // Запуск задачи сканирования.
        App.startTask(new ScanTask());
        dispose();
    }

    private class ScanTask extends Thread {

        @Override
        public void run() {
            try {
                // Запрещаем запуск задач.
                App.mainFrame.tabSource.enableScan(false);
                App.mainFrame.enableProcess(true);
                App.mainFrame.enableCancelProcess(true);
                // Сканирование источника.
                Files.scan(App.srcName, App.srcCamLimit);
                //App.log("SCAN END");
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                // Разрешаем запуск задач.
                App.mainFrame.tabSource.enableScan(true);
                App.mainFrame.enableProcess(true);
                App.mainFrame.enableCancelProcess(false);

                App.mainFrame.tabSource.displayCams();
                App.fireTaskStop();
            }
        }
    }
}
