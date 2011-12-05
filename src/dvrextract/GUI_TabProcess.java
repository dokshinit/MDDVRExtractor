/*
 * Copyright (c) 2011, Aleksey Nikolaevich Dokshin. All right reserved.
 * Contacts: dant.it@gmail.com, dokshin@list.ru.
 */
package dvrextract;

import dvrextract.gui.TitleBorder;
import dvrextract.FFMpeg.Cmd;
import dvrextract.FFMpeg.FFCodec;
import dvrextract.gui.GUI;
import dvrextract.gui.JDateTimeField;
import dvrextract.gui.JExtComboBox;
import dvrextract.gui.JExtComboBox.ExtItem;
import dvrextract.gui.JVScrolledPanel;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import net.miginfocom.swing.MigLayout;

/**
 * Вкладка "Обработка".
 * @author Докшин Алексей Николаевич <dant.it@gmail.com>
 */
public final class GUI_TabProcess extends JPanel implements ActionListener {

    /**
     * Выбор вида настроек "Простые" \ "Расширенные".
     */
    private JCheckBox checkExpert;
    /**
     * Выбранная камера.
     */
    private JTextField textCam;
    /**
     * Период.
     */
    private JDateTimeField dateStart, dateEnd;
    /**
     * Кнопка оценки объёмов.
     */
    private JButton buttonEstimate;
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Путь к файлу-приёмнику.
     */
    private JTextField textDestVideo;
    /**
     * Кнопка выбора файла-приёмника видео.
     */
    private JButton buttonSelectVideo;
    /**
     * Список форматов видео.
     */
    private JExtComboBox comboVideoFormat;
    /**
     * Список разрешений видео.
     */
    private JExtComboBox comboVideoSize;
    /**
     * Список ФПС видео.
     */
    private JExtComboBox comboVideoFPS;
    /**
     * Ручные настройки для кодирования.
     */
    private JTextField textVideoCustom;
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Список режима сохранения аудио.
     */
    private JExtComboBox comboAudioMode;
    /**
     * Путь к файлу-приёмнику-аудио.
     */
    private JTextField textDestAudio;
    /**
     * Кнопка выбора файла-приёмника аудио.
     */
    private JButton buttonSelectAudio;
    /**
     * Формат аудио.
     */
    private JExtComboBox comboAudioFormat;
    /**
     * Ручные настройки для кодирования.
     */
    private JTextField textAudioCustom;
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Список режима сохранения субтитров.
     */
    private JExtComboBox comboSubMode;
    /**
     * Путь к файлу-приёмнику-субтитров.
     */
    private JTextField textDestSub;
    /**
     * Кнопка выбора файла-приёмника субтитров.
     */
    private JButton buttonSelectSub;
    /**
     * Список форматов субтитров.
     */
    private JExtComboBox comboSubFormat;
    /**
     * Скролл для всей закладки (добавляется в панель - единственный компонет в ней).
     */
    private JScrollPane scroll;
    /**
     * Панель скролируемая только вертикально (добавляется в скролл).
     */
    private JVScrolledPanel panel;
    /**
     * Текстовые ресурсы для интерфейса.
     */
    public static String x_Audio, x_Cam, x_Codec, x_CustomOption, x_Evaluate,
            x_File, x_Format, x_FramePerSec, x_Mode, x_NotIndent, x_NotSave,
            x_Period1, x_Period2, x_Select, x_Size, x_Source, x_Sub, x_ToFile,
            x_ToVideo, x_Video, x_WOConvert, x_CalcEnd, x_CalcStart,
            x_SelectDestFile, x_NotePreDecoding, x_NoteSimple;

    /**
     * Конструктор.
     */
    public GUI_TabProcess() {
        panel = new JVScrolledPanel(new MigLayout("ins 5, gap 0, fill", "grow"));
        panel.setOpaque(false); // Не отрисовываем фон - заполнение фоном вьюпорта.
        scroll = new JScrollPane(panel);

        // По умолчанию инициализируется как "простой режим".
        checkExpert = GUI.createCheck("Расширенные настройки", false);
        checkExpert.addActionListener(GUI_TabProcess.this);

        textCam = createText(10);
        textCam.setText(x_NotIndent);
        dateStart = GUI.createDTText();
        dateEnd = GUI.createDTText();
        buttonEstimate = createButton(x_Evaluate);

        textDestVideo = createText(300);
        buttonSelectVideo = createButton(x_Select);
        comboVideoFormat = createCombo();
        comboVideoSize = createCombo();
        comboVideoFPS = createCombo();
        textVideoCustom = createText(300);

        comboAudioMode = createCombo();
        textDestAudio = createText(300);
        buttonSelectAudio = createButton(x_Select);
        comboAudioFormat = createCombo();
        textAudioCustom = createText(300);

        comboSubMode = createCombo();
        textDestSub = createText(300);
        buttonSelectSub = createButton(x_Select);
        comboSubFormat = createCombo();

        ArrayList<FFCodec> list = FFMpeg.getCodecs();
        comboVideoFormat.addItem(0, new Item(x_WOConvert, "copy"));
        int n = 1;
        for (FFCodec i : list) {
            if (i.isEncode && i.isVideo) {
                comboVideoFormat.addItem(n++, new Item(i.title, i.name));
            }
        }
        comboVideoFormat.addItem(1000, new Item(x_CustomOption));
        comboVideoFormat.showData();

        comboVideoFPS.addItem(0, new Item(x_WOConvert, "{origfps}"));
        comboVideoFPS.addItem(1, new Item("12"));
        comboVideoFPS.addItem(2, new Item("25"));
        comboVideoFPS.addItem(1000, new Item(x_CustomOption));
        comboVideoFPS.showData();

        comboVideoSize.addItem(0, new Item(x_WOConvert, "{origsize}"));
        comboVideoSize.addItem(1, new Item("352x288"));
        comboVideoSize.addItem(2, new Item("352x576"));
        comboVideoSize.addItem(3, new Item("704x288"));
        comboVideoSize.addItem(4, new Item("704x576"));
        comboVideoSize.addItem(5, new Item("1280x720"));
        comboVideoSize.addItem(6, new Item("1920x1080"));
        comboVideoSize.addItem(1000, new Item(x_CustomOption));
        comboVideoSize.showData();

        comboAudioMode.addItem(-1, new Item(x_NotSave));
        if (FFMpeg.isAudio_pcm_s16le) {
            comboAudioMode.addItem(0, new Item(x_ToFile));
            comboAudioMode.addItem(1, new Item(x_ToVideo));
        }
        comboAudioMode.showData();

        comboAudioFormat.addItem(0, new Item(x_WOConvert, "copy"));
        n = 1;
        for (FFCodec i : list) {
            if (i.isEncode && i.isAudio) {
                comboAudioFormat.addItem(n++, new Item(i.title, i.name));
            }
        }
        comboAudioFormat.addItem(1000, new Item(x_CustomOption));
        comboAudioFormat.showData();

        comboSubMode.addItem(-1, new Item(x_NotSave));
        comboSubMode.addItem(0, new Item(x_ToFile));
        if (FFMpeg.isSub_srt) {
            comboSubMode.addItem(1, new Item(x_ToVideo));
        }
        comboSubMode.showData();

        comboSubFormat.addItem(0, new Item("SubRip", "srt"));
        comboSubFormat.showData();

        comboVideoFormat.setSelectedId(0);
        comboAudioMode.setSelectedId(FFMpeg.isAudio_pcm_s16le ? 1 : -1);
        comboAudioFormat.setSelectedId(0);
        comboSubMode.setSelectedId(FFMpeg.isSub_srt ? 1 : 0);
        comboSubFormat.setSelectedId(0);

        // TODO: В релизе убрать!
        //setVideoDestination("/home/work/files/AZSVIDEO/1/probe1.mkv");
        //dateStart.setText("01.01.2011 00:00:00");
    }

    /**
     * Создание комбо компонента.
     * @return Компонент.
     */
    private JExtComboBox createCombo() {
        JExtComboBox combo = GUI.createCombo();
        combo.addActionListener(this);
        return combo;
    }

    /**
     * Создание компонента кнопки.
     * @param title Название.
     * @return Компонент.
     */
    private JButton createButton(String title) {
        JButton button = GUI.createButton(title);
        button.addActionListener(this);
        return button;
    }

    /**
     * Создание компонента текстового поля.
     * @param size Длина для расчета размеров.
     * @return Компонент.
     */
    private JTextField createText(int size) {
        JTextField text = GUI.createText(size);
        text.setEditable(false);
        return text;
    }
    /**
     * Окантовка панели названия группы.
     */
    private static final TitleBorder groupTitleBorder = new TitleBorder(new Color(0x808080));
    /**
     * Цвет фона панели названия группы.
     */
    private static final Color groupTitleBackground = new Color(0xC0C0C0);

    /**
     * Добавление панели группы на подложку.
     * @param title Название
     * @return Панель-тело для наполнения группы.
     */
    private JPanel addGroupPanel(String title) {
        JPanel group = new JPanel(new MigLayout("ins 0", "grow", "[]5[]"));

        JPanel gtitle = new JPanel(new MigLayout("ins 3", "grow"));
        gtitle.add(GUI.createLabel("<html><font color=#202020 style='font-size: 12pt; font-weight: bold'>"
                + title + "</font></html>"), "gapleft 5, left");
        gtitle.setBorder(groupTitleBorder);
        gtitle.setBackground(groupTitleBackground);

        JPanel gcontent = new JPanel(new MigLayout("ins 0", "[120:120:120, right][]"));

        group.add(gtitle, "growx, wrap");
        group.add(gcontent, "gapleft 10, gapright 10, gapbottom 5, growx");
        panel.add(group, "spanx, grow, gapbottom 5, wrap");
        return gcontent;
    }

    /**
     * Инициализация графических компонент.
     */
    public void createUI() {
        setLayout(new MigLayout("ins 5, fill", "", "[][grow]"));

        // Для случая смены режима и пересоздания интерфейса.
        panel.removeAll();

        add(checkExpert, "wrap");
        add(scroll, "grow");

        JPanel p1 = addGroupPanel(x_Source);
        p1.add(GUI.createLabel(x_Cam));
        p1.add(textCam, "spanx, wrap");

        p1.add(GUI.createLabel(x_Period1));
        p1.add(dateStart, "w 150, spanx, split 4");
        dateStart.addActionListener(this);
        p1.add(GUI.createLabel(x_Period2));
        p1.add(dateEnd, "w 150");
        dateEnd.addActionListener(this);
        p1.add(buttonEstimate);

        if (checkExpert.isSelected()) { // Режим расширенных настроек.

            JPanel p2 = addGroupPanel(x_Video);
            p2.add(GUI.createLabel(x_File));
            p2.add(textDestVideo, "growx, spanx, split 2");
            p2.add(buttonSelectVideo, "wrap");
            p2.add(GUI.createLabel(x_Codec));
            p2.add(comboVideoFormat, "left, spanx, wrap");
            p2.add(GUI.createLabel(x_Size));
            p2.add(comboVideoSize, "left, spanx, split 3");
            p2.add(GUI.createLabel(x_FramePerSec), "gapleft 20");
            p2.add(comboVideoFPS, "wrap");
            p2.add(GUI.createLabel(x_CustomOption), "");
            p2.add(textVideoCustom, "left, spanx");

            JPanel p3 = addGroupPanel(x_Audio);
            p3.add(GUI.createLabel(x_Mode));
            p3.add(comboAudioMode, "left, spanx, split 3");
            p3.add(textDestAudio, "growx");
            p3.add(buttonSelectAudio, "wrap");
            p3.add(GUI.createLabel(x_Codec), "");
            p3.add(comboAudioFormat, "left, spanx, split 2");
            p3.add(GUI.createNoteLabel(x_NotePreDecoding), "wrap");
            p3.add(GUI.createLabel(x_CustomOption), "");
            p3.add(textAudioCustom, "left, spanx");

            JPanel p4 = addGroupPanel(x_Sub);
            p4.add(GUI.createLabel(x_Mode));
            p4.add(comboSubMode, "left, spanx, split 3");
            p4.add(textDestSub, "growx");
            p4.add(buttonSelectSub, "wrap");
            p4.add(GUI.createLabel(x_Format), "");
            p4.add(comboSubFormat, "left, spanx");

        } else { // Режим упрощенных настроек.

            JPanel p2 = addGroupPanel(x_Video);
            p2.add(GUI.createLabel(x_File));
            p2.add(textDestVideo, "growx, spanx, split 2");
            p2.add(buttonSelectVideo, "wrap");
            p2.add(GUI.createNoteLabel(x_NoteSimple), "skip, spanx");
        }

        fireStartDateChange();
        fireEndDateChange();
        fireVideoFormatSelect();
        fireAudioModeSelect();
        fireAudioFormatSelect();
        fireSubModeSelect();
        fireSubFormatSelect();
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
        } else if (e.getSource() == checkExpert) {
            fireExpertSelect();
        }
    }

    /**
     * Возвращает текущий режим интерфейса закладки.
     * @return Режим: true - экперт, false - простой.
     */
    public boolean isExpert() {
        return checkExpert.isSelected();
    }

    /**
     * Возвращает строку с опциями для видео согласно выбранным опциям.
     * @return Строка опций.
     */
    private Cmd getVideoOptions() {
        Cmd s = new Cmd(false);
        if (checkExpert.isSelected()) { // Режим эксперта.
            boolean isCustom = false;
            ExtItem i = comboVideoFormat.getSelectedItem();
            if (i.id == 1000) {
                isCustom = true;
            } else {
                s.add("-vcodec", ((Item) i.object).name);
            }
            i = comboVideoFPS.getSelectedItem();
            if (i.id == 1000) {
                isCustom = true;
            } else {
                s.add("-r", ((Item) i.object).name);
            }
            i = comboVideoSize.getSelectedItem();
            if (i.id == 1000) {
                isCustom = true;
            } else {
                s.add("-s", ((Item) i.object).name);
            }
            if (isCustom) {
                s.add(textVideoCustom.getText().trim());
            }
        } else {  // Режим упрощенных настроек.
            s.add("-vcodec", "mpeg4", "-r", "{origfps}", "-s", "{origsize}");
        }
        return s;
    }

    /**
     * Возвращает строку с опциями для аудио.
     * @return Строка опций.
     */
    private Cmd getAudioOptions() {
        Cmd s = new Cmd(false);
        if (checkExpert.isSelected()) { // Режим эксперта.
            ExtItem i = comboAudioMode.getSelectedItem();
            if (i.id >= 0) {
                i = comboAudioFormat.getSelectedItem();
                if (i.id == 1000) {
                    s.add(textAudioCustom.getText().trim());
                } else {
                    s.add("-acodec", ((Item) i.object).name);
                }
            }
        } else {  // Режим упрощенных настроек.
            s.add("-acodec", "copy"); // на выходе как раз pcm_s16le, так что без кодирования.
        }
        return s;
    }

    /**
     * Возвращает строку с опциями для субтитров.
     * @return Строка опций.
     */
    private Cmd getSubOptions() {
        Cmd s = new Cmd(false);
        if (checkExpert.isSelected()) { // Режим эксперта.
            ExtItem i = comboSubFormat.getSelectedItem();
            if (i.id >= 0) {
                s.add("-scodec", ((Item) i.object).name);
            }
        } else {  // Режим упрощенных настроек.
        }
        return s;
    }

    /**
     * Отображение названия выбранной на закладке источника камеры.
     * @param title Название камеры.
     */
    public void validateSelectedCam(final String title) {
        GUI.InSwingWait(new Runnable() {

            @Override
            public void run() {
                textCam.setText(title);
            }
        });
    }

    /**
     * Выставление блокировок элементов согласно текущему состоянию.
     */
    public void validateLocks() {
        GUI.InSwingLater(new Runnable() {

            @Override
            public void run() {

                if (Task.isAlive()) {
                    // Выполняется задача.
                    checkExpert.setEnabled(false);
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
                    checkExpert.setEnabled(true);
                    dateStart.setEditable(true);
                    dateEnd.setEditable(true);
                    buttonEstimate.setEnabled(false); //(App.Source.getSelectedCam() > 0) TODO: убрать когда реализую вычисления.
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
        });
    }

    /**
     * Обработка оценки данных за выбранный период (запуск диалога с инфой).
     */
    private void fireEstimate() {
        Task.start(new EstimateTask());
    }

    private void fireStartDateChange() {
        if (dateStart.getTime().after(dateEnd.getTime())) {
            dateEnd.setTime(dateStart.getTime());
        }
        App.Dest.setTimeStart(dateStart.getTime());
    }

    private void fireEndDateChange() {
        if (dateEnd.getTime().before(dateStart.getTime())) {
            dateStart.setTime(dateEnd.getTime());
        }
        App.Dest.setTimeEnd(dateEnd.getTime());
    }

    /**
     * Обработка выбора приёмника (запуск диалога).
     */
    private void fireSelectVideoDestination() {
        SelectVideoDialog dlg = new SelectVideoDialog();
        GUI.centerizeFrame(dlg, App.gui);
        dlg.setVisible(true);
    }

    /**
     * Обработка выбора видеоформата.
     */
    private void fireVideoFormatSelect() {
        App.Dest.setVideoOptions(getVideoOptions());
    }

    /**
     * Обработка выбора приёмника (запуск диалога).
     */
    private void fireSelectAudioDestination() {
        SelectAudioDialog dlg = new SelectAudioDialog();
        GUI.centerizeFrame(dlg, App.gui);
        dlg.setVisible(true);
    }

    /**
     * Обработка выбора аудио режима.
     */
    private void fireAudioModeSelect() {
        if (checkExpert.isSelected()) {
            App.Dest.setAudioType(comboAudioMode.getSelectedItem().id);
        } else {
            App.Dest.setAudioType(1);
        }
    }

    /**
     * Обработка выбора аудиоформата.
     */
    private void fireAudioFormatSelect() {
        App.Dest.setAudioOptions(getAudioOptions());
    }

    /**
     * Обработка выбора приёмника (запуск диалога).
     */
    private void fireSelectSubDestination() {
        SelectSubDialog dlg = new SelectSubDialog();
        GUI.centerizeFrame(dlg, App.gui);
        dlg.setVisible(true);
    }

    /**
     * Обработка выбора режима субтитров.
     */
    private void fireSubModeSelect() {
        if (checkExpert.isSelected()) {
            App.Dest.setSubType(comboSubMode.getSelectedItem().id);
        } else {
            App.Dest.setSubType(0);
        }
    }

    /**
     * Обработка выбора субтитров.
     */
    private void fireSubFormatSelect() {
        App.Dest.setSubOptions(getSubOptions());
    }

    /**
     * Обработка выбора режима закладки.
     */
    private void fireExpertSelect() {
        createUI();
        if (isExpert() == false) {
            App.Dest.setVideoName(App.Dest.getVideoName());
            textDestVideo.setText(App.Dest.getVideoName());
            textDestAudio.setText(App.Dest.getAudioName());
            textDestSub.setText(App.Dest.getSubName());
        }
        revalidate();
    }

    /**
     * Задача - подсчёт примерных данных за введённый период.
     */
    private class EstimateTask extends Task.Thread {

        @Override
        public void task() {
            int cam = App.Source.getSelectedCam();
            if (cam <= 0) {
                return;
            }
            App.gui.startProgress();
            String msg = x_CalcStart;
            App.gui.setProgressInfo(msg);
            App.log(msg);

            // Вычисление приблизительных результатов к обработке.
            CamInfo ci = App.Source.getCamInfo(cam);
            for (FileInfo info : ci.files) {
                // TODO: Подсчёт данных для обработки.
            }

            App.gui.stopProgress();
            msg = x_CalcEnd;
            App.gui.setProgressInfo(msg);
            App.log(msg);

            // TODO: Вывод окна с информацией о данных для обработки.
        }
    }

    /**
     * Воспомогательный класс - для хранения настроек в комбо.
     */
    public static class Item {

        /**
         * Отображаемое название.
         */
        public String title;
        /**
         * Строка с настройками.
         */
        public String name;

        /**
         * Конструктор.
         * @param title Отображаемое название.
         * @param name Имя.
         */
        public Item(String title, String name) {
            this.title = title;
            this.name = name;
        }

        /**
         * Конструктор.
         * @param title Отображаемое название (имя = название).
         */
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

        /**
         * Конструктор.
         */
        private SelectVideoDialog() {
            super(App.gui, x_SelectDestFile,
                    textDestVideo.getText().trim(),
                    checkExpert.isSelected() ? "*.mkv" : "*.avi",
                    Target.NEW_OR_EXIST, Mode.FILE);
        }

        @Override
        public void fireInit(FileChooser fc) {
            FileFilter avi, mkv;
            fc.addChoosableFileFilter(avi = new FileNameExtensionFilter("Video AVI", "avi"));
            fc.addChoosableFileFilter(new FileNameExtensionFilter("Video MPEG4", "mp4", "mpg4", "mpeg4"));
            fc.addChoosableFileFilter(mkv = new FileNameExtensionFilter("Video MKV", "mkv"));
            fc.setFileFilter(isExpert() ? mkv : avi);
        }

        @Override
        public void fireApply(FileChooser fc) throws CancelActionExeption {
            final String name = fc.getSelectedFile().getAbsolutePath();
            App.Dest.setVideoName(name);
            textDestVideo.setText(App.Dest.getVideoName());
            textDestAudio.setText(App.Dest.getAudioName());
            textDestSub.setText(App.Dest.getSubName());
        }
    }

    /**
     * Диалог выбора существующего файла/каталога источника.
     */
    private class SelectAudioDialog extends GUIFileSelectDialog {

        /**
         * Конструктор.
         */
        private SelectAudioDialog() {
            super(App.gui, x_SelectDestFile,
                    textDestAudio.getText().trim(), "*.wav", Target.NEW_OR_EXIST, Mode.FILE);
        }

        @Override
        public void fireInit(FileChooser fc) {
            FileFilter wav;
            fc.addChoosableFileFilter(wav = new FileNameExtensionFilter("Audio WAV", "wav"));
            fc.setFileFilter(wav);
        }

        @Override
        public void fireApply(FileChooser fc) throws CancelActionExeption {
            final String name = fc.getSelectedFile().getAbsolutePath();
            App.Dest.setAudioName(name);
            textDestAudio.setText(name);
        }
    }

    /**
     * Диалог выбора существующего файла/каталога источника.
     */
    private class SelectSubDialog extends GUIFileSelectDialog {

        /**
         * Конструктор.
         */
        private SelectSubDialog() {
            super(App.gui, x_SelectDestFile,
                    textDestSub.getText().trim(), "*.srt", Target.NEW_OR_EXIST, Mode.FILE);
        }

        @Override
        public void fireInit(FileChooser fc) {
            FileFilter srt;
            fc.addChoosableFileFilter(srt = new FileNameExtensionFilter("Subtitle SRT", "srt"));
            fc.setFileFilter(srt);
        }

        @Override
        public void fireApply(FileChooser fc) throws CancelActionExeption {
            final String name = fc.getSelectedFile().getAbsolutePath();
            App.Dest.setSubName(name);
            textDestSub.setText(name);
        }
    }
}
