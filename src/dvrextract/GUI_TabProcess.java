package dvrextract;

import dvrextract.FFMpeg.FFCodec;
import dvrextract.gui.GUI;
import dvrextract.gui.JDateTimeField;
import dvrextract.gui.JExtComboBox;
import dvrextract.gui.JExtComboBox.ExtItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import net.miginfocom.swing.MigLayout;

/**
 * Вкладка "Обработка".
 * @author lex
 */
public final class GUI_TabProcess extends JPanel implements ActionListener {

    // Выбранная камера.
    private JTextField textCam;
    // Период
    private JDateTimeField dateStart, dateEnd;
    // Кнопка оценки объёмов.
    private JButton buttonEstimate;
    // Путь к файлу-приёмнику.
    private JTextField textDestination;
    // Кнопка выбора файла-приёмника.
    private JButton buttonSelect;
    // Список форматов видео.
    private JExtComboBox comboVideoFormat;
    // Список разрешений видео.
    private JExtComboBox comboVideoSize;
    // Список ФПС видео.
    private JExtComboBox comboVideoFPS;
    // Ручные настройки для кодирования.
    private JTextField textVideoCustom;
    // Формат аудио.
    private JExtComboBox comboAudioFormat;
    // Ручные настройки для кодирования.
    private JTextField textAudioCustom;
    // Список форматов субтитров.
    private JExtComboBox comboSubFormat;

    /**
     * Конструктор.
     */
    public GUI_TabProcess() {
        init();
    }

    private void addSection(String title) {
        add(GUI.createSectionLabel(title), "spanx, growx, wrap");
    }

    /**
     * Инициализация графических компонент.
     */
    private void init() {
        setLayout(new MigLayout("", "[10:10:10][right][]", ""));
        addSection("Источник");
        add(GUI.createLabel("Камера"), "skip");
        add(textCam = GUI.createText(10), "spanx, wrap");
        textCam.setEditable(false);
        textCam.setText("не выбрана");

        add(GUI.createLabel("Период c"), "skip");

        add(dateStart = GUI.createDTText(), "w 150, spanx, split 4");
        dateStart.addActionListener(this);
        add(GUI.createLabel("по"), "");
        add(dateEnd = GUI.createDTText(), "w 150");
        dateEnd.addActionListener(this);
        add(buttonEstimate = GUI.createButton("Оценка"), "wrap");
        buttonEstimate.addActionListener(this);

        addSection("Приёмник");
        add(GUI.createLabel("Файл"), "skip");
        add(textDestination = GUI.createText(300), "growx, span, split 2");
        textDestination.setEditable(false);
        add(buttonSelect = GUI.createButton("Выбор"), "wrap");
        buttonSelect.addActionListener(this);

        addSection("Видео");
        add(GUI.createLabel("Кодек"), "skip");
        add(comboVideoFormat = GUI.createCombo(false), "left, spanx, wrap");
        comboVideoFormat.addActionListener(this);
        add(GUI.createLabel("Размер"), "skip");
        add(comboVideoSize = GUI.createCombo(false), "left, spanx, split 3");
        comboVideoSize.addActionListener(this);
        add(GUI.createLabel("Кадр/сек"), "gap 20");
        add(comboVideoFPS = GUI.createCombo(false), "wrap");
        comboVideoFPS.addActionListener(this);
        add(GUI.createLabel("Ручные настройки"), "skip");
        add(textVideoCustom = GUI.createText(300), "left, spanx, wrap");
        textVideoCustom.addActionListener(this);

        addSection("Аудио");
        add(GUI.createLabel("Кодек"), "skip");
        add(comboAudioFormat = GUI.createCombo(false), "left, spanx, wrap");
        comboAudioFormat.addActionListener(this);
        add(GUI.createLabel("Ручные настройки"), "skip");
        add(textAudioCustom = GUI.createText(300), "left, spanx, wrap");

        addSection("Титры");
        add(GUI.createLabel("Формат"), "skip");
        add(comboSubFormat = GUI.createCombo(false), "left, spanx, wrap");

        ArrayList<FFCodec> list = FFMpeg.getCodecs();
        comboVideoFormat.addItem(0, new Item("Без преобразования", "copy"));
        int n = 1;
        for (FFCodec i : list) {
            if (i.isEncode && i.isVideo) {
                comboVideoFormat.addItem(n++, new Item(i.title, i.name));
            }
        }
        comboVideoFormat.addItem(1000, new Item("Ручные настройки"));
        comboVideoFormat.showData();

        comboVideoFPS.addItem(0, new Item("Без преобразования", "{origfps}"));
        comboVideoFPS.addItem(1, new Item("12"));
        comboVideoFPS.addItem(2, new Item("25"));
        comboVideoFPS.addItem(1000, new Item("Ручные настройки"));
        comboVideoFPS.showData();

        comboVideoSize.addItem(0, new Item("Без преобразования", "{origsize}"));
        comboVideoSize.addItem(1, new Item("352x288"));
        comboVideoSize.addItem(2, new Item("352x576"));
        comboVideoSize.addItem(3, new Item("704x288"));
        comboVideoSize.addItem(4, new Item("704x576"));
        comboVideoSize.addItem(1000, new Item("Ручные настройки"));
        comboVideoSize.showData();

        comboAudioFormat.addItem(-1, new Item("Не сохранять"));
        comboAudioFormat.addItem(0, new Item("Без преобразования", "copy"));
        n = 1;
        for (FFCodec i : list) {
            if (i.isEncode && i.isAudio) {
                comboAudioFormat.addItem(n++, new Item(i.title, i.name));
            }
        }
        comboAudioFormat.addItem(1000, new Item("Ручные настройки"));
        comboAudioFormat.showData();

        comboSubFormat.addItem(-1, "Не создавать");
        comboSubFormat.addItem(0, "Отдельный файл");
        comboSubFormat.addItem(1, "Внедрённый поток");
        comboSubFormat.showData();

        comboVideoFormat.setSelectedId(0);
        comboAudioFormat.setSelectedId(0);
        comboSubFormat.setSelectedId(0);
        //
        //dateStart.setText("01.08.2011 10:00:00");
        //dateEnd.setText("01.08.2011 10:59:59");
        setDestination("/home/work/files/probe1.avi");
        fireStartDateChange();
        fireEndDateChange();
        fireVideoFormatSelect();
        fireAudioFormatSelect();
    }

    /**
     * Возвращает строку с опциями для видео согласно выбранным опциям.
     * @param origfps Оригинальное значение fps.
     * @param origsize Оригинальное значение размера кадра вида WxH.
     * @return Строка опций.
     */
    private String getVideoOptions() {
        StringBuilder s = new StringBuilder();
        boolean isCustom = false;
        ExtItem i = comboVideoFormat.getSelectedItem();
        if (i.id == 1000) {
            isCustom = true;
        } else {
            s.append("-vcodec ").append(((Item) i.object).name).append(" ");
        }
        i = comboVideoFPS.getSelectedItem();
        if (i.id == 1000) {
            isCustom = true;
        } else {
            s.append("-r ").append(((Item) i.object).name).append(" ");
        }
        i = comboVideoSize.getSelectedItem();
        if (i.id == 1000) {
            isCustom = true;
        } else {
            s.append("-s ").append(((Item) i.object).name).append(" ");
        }
        if (isCustom) {
            s.append(textVideoCustom.getText().trim());
        }
        return s.toString().trim();
    }

    /**
     * Возвращает строку с опциями для аудио.
     * @return Строка опций.
     */
    private String getAudioOptions() {
        StringBuilder s = new StringBuilder();
        boolean isCustom = false;
        ExtItem i = comboAudioFormat.getSelectedItem();
        if (i.id == 1000) {
            s.append(textAudioCustom.getText().trim());
        } else if (i.id == -1) {
            s.append("-an ");
        } else {
            s.append("-acodec ").append(((Item) i.object).name).append(" ");
        }
        return s.toString().trim();
    }

    /**
     * Устанавливает имя выходного файла (без проверки!). 
     * @param text 
     */
    public void setDestination(String text) {
        App.destName = text;
        textDestination.setText(text);
    }

    /**
     * Отображение выбранной на закладке источника камере.
     */
    public void displayCam(String title) {
        textCam.setText(title);
    }

    /**
     * Выставление блокировок элементов согласно текущему состоянию.
     */
    public void setLocks() {
        if (Task.isAlive()) {
            // Выполняется задача.
            dateStart.setEditable(false);
            dateEnd.setEditable(false);
            buttonEstimate.setEnabled(false);
            buttonSelect.setEnabled(false);
            comboVideoFormat.setEnabled(false);
            comboVideoSize.setEnabled(false);
            comboVideoFPS.setEnabled(false);
            textVideoCustom.setEnabled(false);
            comboAudioFormat.setEnabled(false);
            comboSubFormat.setEnabled(false);
            textAudioCustom.setEnabled(false);
        } else {
            // Задач нет.
            dateStart.setEditable(true);
            dateEnd.setEditable(true);
            buttonEstimate.setEnabled(App.srcCamSelect > 0);
            buttonSelect.setEnabled(true);
            comboVideoFormat.setEnabled(true);
            ExtItem i = comboVideoFormat.getSelectedItem();
            if (i != null && (i.id == 0 || i.id == 1000)) {
                // Без обработки/вручную - сбрасываем остальные комбо и лочим.
                comboVideoSize.setSelectedId(i.id);
                comboVideoSize.setEnabled(false);
                comboVideoFPS.setSelectedId(i.id);
                comboVideoFPS.setEnabled(false);
                textVideoCustom.setEnabled(i.id == 1000);
            } else {
                comboVideoSize.setEnabled(true);
                comboVideoFPS.setEnabled(true);
                textVideoCustom.setEnabled(false);
            }
            comboAudioFormat.setEnabled(true);
            i = comboAudioFormat.getSelectedItem();
            textAudioCustom.setEnabled(i != null && i.id == 1000);
            comboSubFormat.setEnabled(true);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == dateStart) {
            fireStartDateChange();
        } else if (e.getSource() == dateEnd) {
            fireEndDateChange();
        } else if (e.getSource() == buttonEstimate) {
            fireEstimate();
        } else if (e.getSource() == buttonSelect) {
            fireSelectDestination();
        } else if (e.getSource() == comboVideoFormat
                || e.getSource() == comboVideoSize
                || e.getSource() == comboVideoFPS) {
            fireVideoFormatSelect();
        } else if (e.getSource() == comboAudioFormat) {
            fireAudioFormatSelect();
        }
    }

    /**
     * Обработка оценки данных за выбранный период (запуск диалога с инфой).
     */
    private void fireEstimate() {
        Task.start(new EstimateTask());
    }

    /**
     * Задача - подсчёт примерных данных за введённый период.
     */
    private class EstimateTask extends Task.Thread {

        @Override
        public void task() {
            // Вычисление приблизительных результатов обработки.
            if (App.srcCamSelect < 0) {
                return;
            }
            CamInfo ci = App.srcCams[App.srcCamSelect];
            for (FileInfo info : ci.files ) {
                
            }
        }
    }

    private void fireStartDateChange() {
        if (dateStart.getTime().after(dateEnd.getTime())) {
            dateEnd.setTime(dateStart.getTime());
        }
        App.destTimeStart = dateStart.getTime();
        //System.out.println("Fire Start Change dt="+App.destTimeStart.toString());
    }

    private void fireEndDateChange() {
        if (dateEnd.getTime().before(dateStart.getTime())) {
            dateStart.setTime(dateEnd.getTime());
        }
        App.destTimeEnd = dateStart.getTime();
        //System.out.println("Fire End Change dt="+App.destTimeEnd.toString());
    }

    /**
     * Обработка выбора приёмника (запуск диалога).
     */
    private void fireSelectDestination() {
        SelectDialog dlg = new SelectDialog();
        dlg.center();
        dlg.setVisible(true);
    }

    /**
     * Обработка выбора видеоформата.
     */
    private void fireVideoFormatSelect() {
        setLocks();
        App.videoOptions = getVideoOptions();
    }

    /**
     * Обработка выбора аудиоформата.
     */
    private void fireAudioFormatSelect() {
        setLocks();
        App.audioOptions = getAudioOptions();
    }

    /**
     * Воспомогательный класс - для хранения настроек в комбо.
     */
    public static class Item {

        // Отображаемое название.
        public String title;
        // Строка с настройками.
        public String name;

        public Item(String title, String name) {
            this.title = title;
            this.name = name;
        }

        public Item(String title) {
            this.title = title;
            this.name = title;
        }

        @Override
        public String toString() {
            return title;
        }
    }

    /**
     * Диалог выбора существующего файла/каталога источника.
     */
    private class SelectDialog extends GUIFileSelectDialog {

        private SelectDialog() {
            super(App.mainFrame, "Выбор файла приёмника",
                    textDestination.getText().trim(), Target.NEW_OR_EXIST, Mode.FILE);
        }

        @Override
        public void fireApply(FileChooser fc) throws CancelActionExeption {
            final File f = fc.getSelectedFile();
            setDestination(f.getAbsolutePath());
        }
    }
}
