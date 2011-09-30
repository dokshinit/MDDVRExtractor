package dvrextract;

import dvrextract.gui.GUI;
import dvrextract.gui.JExtComboBox;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
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
    // Путь к файлу-приёмнику.
    private JTextField textDestination;
    // Кнопка выбора файла-приёмника.
    private JButton buttonSelect;
    // Конвертировать видео?
    private JCheckBox checkVideoConvert;
    // Список форматов видео.
    private JExtComboBox comboVideoFormat;
    // Список разрешений видео.
    private JExtComboBox comboVideoSize;
    // Список ФПС видео.
    private JExtComboBox comboVideoFPS;
    // Включать аудио.
    private JCheckBox checkAudio;
    // Конвертировать аудио.
    private JCheckBox checkAudioConvert;
    // Формат аудио.
    private JExtComboBox comboAudioFormat;
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

        add(dateStart = GUI.createFormattedText(formatter), "w 150, spanx, split 3");
        add(GUI.createLabel("по"), "");
        add(dateStart = GUI.createFormattedText(formatter), "w 150, wrap");

        addSection("Приёмник");
        add(GUI.createLabel("Файл"), "skip");
        add(textDestination = GUI.createText(300), "growx, span, split 2");
        textDestination.setEditable(false);
        add(buttonSelect = GUI.createButton("Выбор"), "wrap");
        buttonSelect.addActionListener(this);

        addSection("Видео");
        add(checkVideoConvert = GUI.createCheck("Конвертировать (без конверсии значительно быстрее).", false), "left, skip, spanx");
        
        addSection("Аудио");
        add(checkAudio = GUI.createCheck("Сохранять если есть.", true), "left, skip, spanx, split 2");
        add(checkAudioConvert = GUI.createCheck("Конвертировать (без конверсии значительно быстрее).", false), "wrap");

        addSection("Титры");
        add(checkSub = GUI.createCheck("Создавать титры.", true), "left, skip, spanx, split 2");
        add(checkSubDetail = GUI.createCheck("Детализированные титры.", false), "wrap");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == buttonSelect) {
            fireSelectDestination();
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

}
