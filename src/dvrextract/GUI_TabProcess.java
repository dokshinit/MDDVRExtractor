package dvrextract;

import dvrextract.FFMpeg.FFCodec;
import dvrextract.gui.GUI;
import dvrextract.gui.JExtComboBox;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.MaskFormatter;
import net.miginfocom.swing.MigLayout;

/**
 * Вкладка "Обработка".
 * @author lex
 */
public final class GUI_TabProcess extends JPanel implements ActionListener {

    // Выбранная камера.
    private JTextField textCam;
    // Период
    private JFormattedTextField dateStart, dateEnd;
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
    // Включить субтитры.
    private JCheckBox checkSub;
    // Подробные субтитры.
    private JCheckBox checkSubDetail;

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
        add(GUI.createLabel("Период c"), "skip");
        MaskFormatter formatter = null;
        try {
            formatter = new MaskFormatter("##.##.#### ##:##:##");
            formatter.setPlaceholderCharacter('_');
        } catch (ParseException ex) {
        }

        add(dateStart = GUI.createFormattedText(formatter), "w 150, spanx, split 4");
        add(GUI.createLabel("по"), "");
        add(dateEnd = GUI.createFormattedText(formatter), "w 150");
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
        add(GUI.createLabel("Размер"), "skip");
        add(comboVideoSize = GUI.createCombo(false), "left, spanx, split 3");
        add(GUI.createLabel("Кадр/сек"), "gap 20");
        add(comboVideoFPS = GUI.createCombo(false), "wrap");
        add(GUI.createLabel("Ручные настройки"), "skip");
        add(textVideoCustom = GUI.createText(300), "left, spanx, wrap");

        addSection("Аудио");
        add(GUI.createLabel("Кодек"), "skip");
        add(comboAudioFormat = GUI.createCombo(false), "left, spanx, wrap");
        add(GUI.createLabel("Ручные настройки"), "skip");
        add(textAudioCustom = GUI.createText(300), "left, spanx, wrap");

        addSection("Титры");
        add(checkSub = GUI.createCheck("Создавать титры.", true), "left, skip, spanx, split 2");
        add(checkSubDetail = GUI.createCheck("Детализированные титры.", false), "wrap");

        ArrayList<FFCodec> list = FFMpeg.getCodecs();
        comboVideoFormat.removeItems();
        comboVideoFormat.addItem(0, new Item("Без преобразования"));
        int n = 1;
        for (FFCodec i : list) {
            if (i.isEncode && i.isVideo) {
                comboVideoFormat.addItem(n++, new Item(i.title, i.name));
            }
        }
        comboVideoFormat.addItem(100, new Item("Ручные настройки"));
        comboVideoFormat.showData();

        comboVideoFPS.addItem(0, new Item("Без преобразования"));
        comboVideoFPS.addItem(1, new Item("12"));
        comboVideoFPS.addItem(2, new Item("25"));
        comboVideoFPS.addItem(100, new Item("Ручные настройки"));
        comboVideoFPS.showData();

        comboVideoSize.addItem(0, new Item("Без преобразования"));
        comboVideoSize.addItem(1, new Item("352x288"));
        comboVideoSize.addItem(2, new Item("352x576"));
        comboVideoSize.addItem(3, new Item("704x288"));
        comboVideoSize.addItem(4, new Item("704x576"));
        comboVideoSize.addItem(100, new Item("Ручные настройки"));
        comboVideoSize.showData();

        comboAudioFormat.addItem(-1, "Не сохранять");
        comboAudioFormat.addItem(0, "Без преобразования");
        n = 1;
        for (FFCodec i : list) {
            if (i.isEncode && i.isAudio) {
                comboAudioFormat.addItem(n++, new Item(i.title, i.name));
            }
        }
        comboAudioFormat.addItem(100, "Ручные настройки");
        comboAudioFormat.showData();
        
        //
        dateStart.setText("01.08.2011 10:00:00");
        dateEnd.setText("01.08.2011 10:59:59");
        textDestination.setText("/home/work/files/probe1.avi");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == buttonEstimate) {
            fireEstimate();
        } else if (e.getSource() == buttonSelect) {
            fireSelectDestination();
        }
    }

    /**
     * Обработка оценки данных за выбранный период (запуск диалога с инфой).
     */
    private void fireEstimate() {
        App.startTask(new EstimateTask());
    }
    
    /**
     * Задача - подсчёт примерных данных за введенный период.
     */
    private class EstimateTask extends Thread {

        @Override
        public void run() {
            try {
                // Запрещаем запуск задач.
                App.mainFrame.tabSource.enableScan(false);
                App.mainFrame.enableProcess(true);
                App.mainFrame.enableCancelProcess(true);
                // Сканирование источника.
                //Files.scan(App.srcName, App.srcCamLimit);
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                // Разрешаем запуск задач.
                App.mainFrame.tabSource.enableScan(true);
                App.mainFrame.enableProcess(true);
                App.mainFrame.enableCancelProcess(false);
                App.fireTaskStop();
            }
        }
    }        

    /**
     * Обработка выбора приёмника (запуск диалога).
     */
    private void fireSelectDestination() {
//        GUI_SourceSelect dlg = new GUI_SourceSelect();
//        dlg.center();
//        dlg.setVisible(true);
//        // Отображаем новый источник и его тип.
//        textSource.setText(App.srcName);
//        textType.setText(App.srcType.title);
    }

    /**
     * Воспомогательный класс - для хранения настроек в комбо.
     */
    public class Item {

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
            this.name = "";
        }

        @Override
        public String toString() {
            return title;
        }
    }
}
