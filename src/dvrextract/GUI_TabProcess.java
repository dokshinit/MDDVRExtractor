package dvrextract;

import dvrextract.FFMpeg.FFCodec;
import dvrextract.gui.GUI;
import dvrextract.gui.JDateTimeField;
import dvrextract.gui.JExtComboBox;
import dvrextract.gui.JExtComboBox.ExtItem;
import dvrextract.gui.JVScrolledPanel;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
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
    ////////////////////////////////////////////////////////////////////////////
    // Путь к файлу-приёмнику.
    private JTextField textDestVideo;
    // Кнопка выбора файла-приёмника видео.
    private JButton buttonSelectVideo;
    // Список форматов видео.
    private JExtComboBox comboVideoFormat;
    // Список разрешений видео.
    private JExtComboBox comboVideoSize;
    // Список ФПС видео.
    private JExtComboBox comboVideoFPS;
    // Ручные настройки для кодирования.
    private JTextField textVideoCustom;
    ////////////////////////////////////////////////////////////////////////////
    // Список режима сохранения аудио.
    private JExtComboBox comboAudioMode;
    // Путь к файлу-приёмнику-аудио.
    private JTextField textDestAudio;
    // Кнопка выбора файла-приёмника аудио.
    private JButton buttonSelectAudio;
    // Формат аудио.
    private JExtComboBox comboAudioFormat;
    // Ручные настройки для кодирования.
    private JTextField textAudioCustom;
    ////////////////////////////////////////////////////////////////////////////
    // Список режима сохранения субтитров.
    private JExtComboBox comboSubMode;
    // Путь к файлу-приёмнику-субтитров.
    private JTextField textDestSub;
    // Кнопка выбора файла-приёмника субтитров.
    private JButton buttonSelectSub;
    // Список форматов субтитров.
    private JExtComboBox comboSubFormat;

    /**
     * Конструктор.
     */
    public GUI_TabProcess() {
        init();
    }

    private JExtComboBox addCombo(JPanel p, String layparam) {
        JExtComboBox c = GUI.createCombo(false);
        p.add(c, layparam == null ? "" : layparam);
        c.addActionListener(this);
        return c;
    }

    private static final TitleBorder titleBorder = new TitleBorder(new Color(0x808080));
    private static final Color panelBg = new Color(0xfffef6);
    private static final Color titleBg = new Color(0xC0C0C0);
    private static final Color scrollBg = new Color(0xD8D8D0); //0xe8e5d9
    
    JPanel addPanel(String s, String pBcond) {
        JPanel p = new JPanel(new MigLayout("ins 0", "grow", "[]5[]"));
        //p.setBackground(panelBg);

        JPanel pT = new JPanel(new MigLayout("ins 3", "grow"));
        pT.add(GUI.createLabel("<html><font color=#202020 style='font-size: 12pt; font-weight: bold'>" + s + "</font></html>"), "gapleft 5, left");
        pT.setBorder(titleBorder);
        pT.setBackground(titleBg);

        JPanel pB = new JPanel(new MigLayout("ins 0", "[120:120:120, right][]"));
        //pB.setBackground(panelBg);

        p.add(pT, "growx, wrap");
        p.add(pB, "gapleft 10, gapright 10, gapbottom 5, growx");
        panel.add(p, "spanx, grow, gapbottom 5, wrap");
        return pB;
    }

    void addLine(JPanel p, String s1, String s2) {
        p.add(GUI.createLabel("<html><b>" + s1 + "</b></html>"), "right, top");
        p.add(GUI.createLabel("<html>" + s2 + "</html>"), "left, top, wrap");
    }

    void addLine(JPanel p, String s) {
        p.add(GUI.createLabel("<html>" + s + "</html>"), "spanx, top, wrap");
    }
    JScrollPane scroll;
    JVScrolledPanel panel;

    /**
     * Инициализация графических компонент.
     */
    private void init() {
        setLayout(new MigLayout("ins 5, fill"));
        panel = new JVScrolledPanel(new MigLayout("ins 5, gap 0, fill", "grow"));
        panel.setOpaque(false); // Не отрисовываем фон - заполнение фоном вьюпорта.
        scroll = new JScrollPane(panel);
        //panel.setBackground(new Color(0xE0E0E0));
        //scroll.getViewport().setBackground(scrollBg);
        add(scroll, "grow");
        
        JPanel p1 = addPanel("Источник", "");
        p1.add(GUI.createLabel("Камера"), "");
        p1.add(textCam = GUI.createText(10), "spanx, wrap");
        textCam.setEditable(false);
        textCam.setText("не выбрана");

        p1.add(GUI.createLabel("Период c"), "");
        p1.add(dateStart = GUI.createDTText(), "w 150, spanx, split 4");
        dateStart.addActionListener(this);
        p1.add(GUI.createLabel("по"), "");
        p1.add(dateEnd = GUI.createDTText(), "w 150");
        dateEnd.addActionListener(this);
        p1.add(buttonEstimate = GUI.createButton("Оценка"));
        buttonEstimate.addActionListener(this);
        
        JPanel p2 = addPanel("Видео", "");
        p2.add(GUI.createLabel("Файл"), "");
        p2.add(textDestVideo = GUI.createText(300), "growx, spanx, split 2");
        textDestVideo.setEditable(false);
        p2.add(buttonSelectVideo = GUI.createButton("Выбор"), "wrap");
        buttonSelectVideo.addActionListener(this);
        p2.add(GUI.createLabel("Кодек"), "");
        comboVideoFormat = addCombo(p2, "left, spanx, wrap");
        p2.add(GUI.createLabel("Размер"), "");
        comboVideoSize = addCombo(p2, "left, spanx, split 3");
        p2.add(GUI.createLabel("Кадр/сек"), "gapleft 20");
        comboVideoFPS = addCombo(p2, "wrap");
        p2.add(GUI.createLabel("Ручные настройки"), "");
        p2.add(textVideoCustom = GUI.createText(300), "left, spanx");
        textVideoCustom.addActionListener(this);

        JPanel p3 = addPanel("Аудио", "");
        p3.add(GUI.createLabel("Режим"), "");
        comboAudioMode = addCombo(p3, "left, spanx, split 3");
        p3.add(textDestAudio = GUI.createText(300), "growx");
        textDestAudio.setEditable(false);
        p3.add(buttonSelectAudio = GUI.createButton("Выбор"), "wrap");
        buttonSelectAudio.addActionListener(this);
        p3.add(GUI.createLabel("Кодек"), "");
        comboAudioFormat = addCombo(p3, "left, spanx, wrap");
        p3.add(GUI.createLabel("Ручные настройки"), "");
        p3.add(textAudioCustom = GUI.createText(300), "left, spanx");

        JPanel p4 = addPanel("Титры", "");
        p4.add(GUI.createLabel("Режим"), "");
        comboSubMode = addCombo(p4, "left, spanx, split 3");
        p4.add(textDestSub = GUI.createText(300), "growx");
        textDestSub.setEditable(false);
        p4.add(buttonSelectSub = GUI.createButton("Выбор"), "wrap");
        buttonSelectSub.addActionListener(this);
        p4.add(GUI.createLabel("Формат"), "");
        comboSubFormat = addCombo(p4, "left, spanx");

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
        comboVideoSize.addItem(5, new Item("1280x720"));
        comboVideoSize.addItem(6, new Item("1920x1080"));
        comboVideoSize.addItem(1000, new Item("Ручные настройки"));
        comboVideoSize.showData();

        comboAudioMode.addItem(-1, new Item("Не сохранять"));
        if (FFMpeg.isAudio_g722) {
            comboAudioMode.addItem(0, new Item("В файл"));
            comboAudioMode.addItem(1, new Item("В видео"));
        }
        comboAudioMode.showData();

        comboAudioFormat.addItem(0, new Item("Без преобразования", "copy"));
        n = 1;
        for (FFCodec i : list) {
            if (i.isEncode && i.isAudio) {
                comboAudioFormat.addItem(n++, new Item(i.title, i.name));
            }
        }
        comboAudioFormat.addItem(1000, new Item("Ручные настройки"));
        comboAudioFormat.showData();

        comboSubMode.addItem(-1, new Item("Не создавать"));
        comboSubMode.addItem(0, new Item("В файл"));
        if (FFMpeg.isSub_srt) {
            comboSubMode.addItem(1, new Item("В видео"));
        }
        comboSubMode.showData();

        comboSubFormat.addItem(0, new Item("SubRip", "srt"));
        comboSubFormat.showData();

        comboVideoFormat.setSelectedId(0);
        comboAudioMode.setSelectedId(FFMpeg.isAudio_g722 ? 1 : -1);
        comboAudioFormat.setSelectedId(0);
        comboSubMode.setSelectedId(FFMpeg.isSub_srt ? 1 : 0);
        comboSubFormat.setSelectedId(0);
        //
        setVideoDestination("/home/work/files/AZSVIDEO/1/probe1.mkv");
        dateStart.setText("01.01.2011 00:00:00");

        fireStartDateChange();
        fireEndDateChange();
        fireVideoFormatSelect();
        fireAudioFormatSelect();
        fireSubFormatSelect();
    }

    /**
     * Возвращает строку с опциями для видео согласно выбранным опциям.
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
        ExtItem i = comboAudioMode.getSelectedItem();
        if (i.id >= 0) {
            i = comboAudioFormat.getSelectedItem();
            if (i.id == 1000) {
                s.append(textAudioCustom.getText().trim());
            } else {
                s.append("-acodec ").append(((Item) i.object).name).append(" ");
            }
        }
        return s.toString().trim();
    }

    /**
     * Возвращает строку с опциями для субтитров.
     * @return Строка опций.
     */
    private String getSubOptions() {
        StringBuilder s = new StringBuilder();
        ExtItem i = comboSubFormat.getSelectedItem();
        if (i.id >= 0) {
            s.append("-scodec ").append(((Item) i.object).name).append(" ");
        }
        return s.toString().trim();
    }

    /**
     * Устанавливает имя выходного файла (без проверки!). 
     * @param text Путь и имя файла.
     */
    public void setVideoDestination(String text) {
        App.destVideoName = text;
        textDestVideo.setText(text);
        setAudioDestination(Files.getNameWOExt(App.destVideoName) + ".wav");
        setSubDestination(Files.getNameWOExt(App.destVideoName) + ".srt");
    }

    /**
     * Устанавливает имя выходного файла (без проверки!). 
     * @param text Путь и имя файла.
     */
    public void setAudioDestination(String text) {
        App.destAudioName = text;
        textDestAudio.setText(text);
    }

    /**
     * Устанавливает имя выходного файла (без проверки!). 
     * @param text Путь и имя файла.
     */
    public void setSubDestination(String text) {
        App.destSubName = text;
        textDestSub.setText(text);
    }

    /**
     * Отображение выбранной на закладке источника камере.
     * @param title Название камеры.
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
            buttonSelectVideo.setEnabled(false);
            comboVideoFormat.setEnabled(false);
            comboVideoSize.setEnabled(false);
            comboVideoFPS.setEnabled(false);
            textVideoCustom.setEnabled(false);
            buttonSelectAudio.setEnabled(false);
            comboAudioMode.setEnabled(false);
            comboAudioFormat.setEnabled(false);
            textAudioCustom.setEnabled(false);
            buttonSelectSub.setEnabled(false);
            comboSubMode.setEnabled(false);
            comboSubFormat.setEnabled(false);
        } else {
            // Задач нет.
            dateStart.setEditable(true);
            dateEnd.setEditable(true);
            buttonEstimate.setEnabled(false); //App.srcCamSelect > 0 TODO убрать когда реализую вычисления.
            buttonSelectVideo.setEnabled(true);
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
            i = comboAudioMode.getSelectedItem();
            boolean m = i != null && i.id == 0 ? true : false;
            buttonSelectAudio.setEnabled(m);
            buttonSelectAudio.setVisible(m);
            textDestAudio.setVisible(m);
            comboAudioMode.setEnabled(true);
            i = comboAudioMode.getSelectedItem();
            if (i != null && i.id == -1) {
                comboAudioFormat.setEnabled(false);
                textAudioCustom.setEditable(false);
            } else {
                comboAudioFormat.setEnabled(true);
                i = comboAudioFormat.getSelectedItem();
                textAudioCustom.setEnabled(i != null && i.id == 1000);
            }
            i = comboSubMode.getSelectedItem();
            m = i != null && i.id == 0 ? true : false;
            buttonSelectSub.setEnabled(m);
            buttonSelectSub.setVisible(m);
            textDestSub.setVisible(m);
            comboSubMode.setEnabled(true);
            i = comboSubMode.getSelectedItem();
            comboSubFormat.setEnabled(i != null && i.id == -1 ? false : true);
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
        } else if (e.getSource() == buttonSelectVideo) {
            fireSelectVideoDestination();
        } else if (e.getSource() == comboVideoFormat
                || e.getSource() == comboVideoSize
                || e.getSource() == comboVideoFPS) {
            fireVideoFormatSelect();
        } else if (e.getSource() == comboAudioMode) {
            fireAudioModeSelect();
        } else if (e.getSource() == buttonSelectAudio) {
            fireSelectAudioDestination();
        } else if (e.getSource() == comboAudioFormat) {
            fireAudioFormatSelect();
        } else if (e.getSource() == comboSubMode) {
            fireSubModeSelect();
        } else if (e.getSource() == buttonSelectSub) {
            fireSelectSubDestination();
        } else if (e.getSource() == comboSubFormat) {
            fireSubFormatSelect();
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
            App.mainFrame.pack();
            
            if (App.srcCamSelect < 0) {
                return;
            }
            App.mainFrame.startProgress();
            String msg = "Подсчёт данных для обработки...";
            App.mainFrame.setProgressInfo(msg);
            App.log(msg);

            // Вычисление приблизительных результатов к обработке.
            CamInfo ci = App.srcCams[App.srcCamSelect];
            for (FileInfo info : ci.files) {
                // TODO: Подсчёт данных для обработки.
            }

            App.mainFrame.stopProgress();
            msg = "Подсчёт данных для обработки завершён.";
            App.mainFrame.setProgressInfo(msg);
            App.log(msg);

            // TODO: Вывод окна с информацией о данных для обработки.
        }
    }

    private void fireStartDateChange() {
        if (dateStart.getTime().after(dateEnd.getTime())) {
            dateEnd.setTime(dateStart.getTime());
        }
        App.destTimeStart = dateStart.getTime();
    }

    private void fireEndDateChange() {
        if (dateEnd.getTime().before(dateStart.getTime())) {
            dateStart.setTime(dateEnd.getTime());
        }
        App.destTimeEnd = dateEnd.getTime();
    }

    /**
     * Обработка выбора приёмника (запуск диалога).
     */
    private void fireSelectVideoDestination() {
        SelectVideoDialog dlg = new SelectVideoDialog();
        GUI.centerizeFrame(dlg, App.mainFrame);
        dlg.setVisible(true);
    }

    /**
     * Обработка выбора видеоформата.
     */
    private void fireVideoFormatSelect() {
        setLocks();
        App.destVideoOptions = getVideoOptions();
    }

    /**
     * Обработка выбора приёмника (запуск диалога).
     */
    private void fireSelectAudioDestination() {
        SelectAudioDialog dlg = new SelectAudioDialog();
        GUI.centerizeFrame(dlg, App.mainFrame);
        dlg.setVisible(true);
    }

    /**
     * Обработка выбора аудио режима.
     */
    private void fireAudioModeSelect() {
        setLocks();
        App.destAudioType = comboAudioMode.getSelectedItem().id;
    }

    /**
     * Обработка выбора аудиоформата.
     */
    private void fireAudioFormatSelect() {
        setLocks();
        App.destAudioOptions = getAudioOptions();
    }

    /**
     * Обработка выбора приёмника (запуск диалога).
     */
    private void fireSelectSubDestination() {
        SelectSubDialog dlg = new SelectSubDialog();
        GUI.centerizeFrame(dlg, App.mainFrame);
        dlg.setVisible(true);
    }

    /**
     * Обработка выбора режима субтитров.
     */
    private void fireSubModeSelect() {
        setLocks();
        App.destSubType = comboSubMode.getSelectedItem().id;
    }

    /**
     * Обработка выбора субтитров.
     */
    private void fireSubFormatSelect() {
        setLocks();
        App.destSubOptions = getSubOptions();
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
    private class SelectVideoDialog extends GUIFileSelectDialog {

        private SelectVideoDialog() {
            super(App.mainFrame, "Выбор файла приёмника",
                    textDestVideo.getText().trim(), Target.NEW_OR_EXIST, Mode.FILE);
        }

        @Override
        public void fireApply(FileChooser fc) throws CancelActionExeption {
            final File f = fc.getSelectedFile();
            setVideoDestination(f.getAbsolutePath());
        }
    }

    /**
     * Диалог выбора существующего файла/каталога источника.
     */
    private class SelectAudioDialog extends GUIFileSelectDialog {

        private SelectAudioDialog() {
            super(App.mainFrame, "Выбор файла приёмника",
                    textDestAudio.getText().trim(), Target.NEW_OR_EXIST, Mode.FILE);
        }

        @Override
        public void fireApply(FileChooser fc) throws CancelActionExeption {
            final File f = fc.getSelectedFile();
            setAudioDestination(f.getAbsolutePath());
        }
    }

    /**
     * Диалог выбора существующего файла/каталога источника.
     */
    private class SelectSubDialog extends GUIFileSelectDialog {

        private SelectSubDialog() {
            super(App.mainFrame, "Выбор файла приёмника",
                    textDestSub.getText().trim(), Target.NEW_OR_EXIST, Mode.FILE);
        }

        @Override
        public void fireApply(FileChooser fc) throws CancelActionExeption {
            final File f = fc.getSelectedFile();
            setSubDestination(f.getAbsolutePath());
        }
    }
}
