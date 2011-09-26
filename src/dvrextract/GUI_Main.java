package dvrextract;

import dvrextract.gui.GUI;
import dvrextract.gui.GUITabPane;
import dvrextract.gui.GUIFrame;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import javax.swing.*;
import net.miginfocom.swing.MigLayout;

/**
 * Основное окно приложения.
 * @author lex
 */
public final class GUI_Main extends GUIFrame implements ActionListener {

    ////////////////////////////////////////////////////////////////////////////
    // Закладка: Источник
    ////////////////////////////////////////////////////////////////////////////
    public GUI_TabSource tabSource;
    public GUI_TabProcess tabProcess;
    ////////////////////////////////////////////////////////////////////////////
    // Закладка: Обработка
    ////////////////////////////////////////////////////////////////////////////
    private JCheckBox checkRaw; // Создавать сырой файл?
    private JTextField textRaw; // Путь и имя файла.
    private JButton buttonRaw; // Выбор файла.
    //
    private JCheckBox checkVideo; // Создавать видео?
    private JTextField textVideo; // Путь и имя файла.
    private JButton buttonVideo; // Выбор файла.
    private JCheckBox checkSub; // Делать субтитры к видео?
    //
    private JComboBox comboCams; // Выбор камеры из списка.
    private JCheckBox checkAudio; // Обрабатывать аудио поток.
    ////////////////////////////////////////////////////////////////////////////
    // На всех вкладках
    ////////////////////////////////////////////////////////////////////////////
    private JTextField textInfo; // Строка состояния обработки.
    private JButton buttonProcess; // Запуск\остановка обработки.
    private JProgressBar progressBar; // Прогрес выполнения операции.

    /**
     * Конструктор.
     */
    public GUI_Main() {
        init();
    }

    /**
     * Инициализация графических компонентов.
     */
    private void init() {
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setPreferredSize(new Dimension(900, 600));

        GUITabPane tabPane = new GUITabPane();

        ////////////////////////////////////////////////////////////////////////
        // Вкладка "Источник"
        ////////////////////////////////////////////////////////////////////////
        tabPane.addTab("Источник", tabSource = new GUI_TabSource());

        ////////////////////////////////////////////////////////////////////////
        // Вкладка "Обработка"
        ////////////////////////////////////////////////////////////////////////
        tabPane.addTab("Обработка", tabProcess = new GUI_TabProcess());

        ////////////////////////////////////////////////////////////////////////
        // Вкладка "Состояние" (обработки)
        ////////////////////////////////////////////////////////////////////////
        JPanel panelTab3 = new JPanel(new MigLayout());
        tabPane.addTab("Состояние", panelTab3);

        ////////////////////////////////////////////////////////////////////////
        // Вкладка "Лог" (обработки)
        ////////////////////////////////////////////////////////////////////////
        JPanel panelTab4 = new JPanel(new MigLayout());
        tabPane.addTab("Лог", panelTab4);

        textVideo = new JTextField(50);
        buttonVideo = GUI.createButton("Выбор");

        comboCams = new JComboBox();
        checkAudio = new JCheckBox("Включать аудиоданные.");

        buttonProcess = GUI.createButton("Обработка");
        buttonProcess.addActionListener(this);

        progressBar = new JProgressBar();
        progressBar.setPreferredSize(new Dimension(100, 20));

        add(tabPane, BorderLayout.CENTER);

        JPanel panelButton = new JPanel(new MigLayout("", "[][grow,fill][10px]"));

        panelButton.add(GUI.createLabel("Инфо:"));
        panelButton.add(textInfo = GUI.createText(100), "");
        textInfo.setEditable(false);
        textInfo.setBackground(new Color(0xE0E0E0));
        panelButton.add(buttonProcess, "spany 2, growy, wrap");
        panelButton.add(progressBar, "spanx 2, growx");
        add(panelButton, BorderLayout.SOUTH);

        pack();
    }
    // Сосотояние кнопки "Обработка".
    // Если true - "Отмена" (остановка текущей задачи), false - "Обработка" (запуск обработки).
    private boolean cancelState = true;

    /**
     * Разрешение/запрет запуска обработки.
     * @param state Статус разрешения.
     */
    public void enableProcess(boolean state) {
        buttonProcess.setEnabled(state);
    }

    /**
     * Установка режима срабатывания кнопки "Обработка".
     * @param state true - "Отмена", false - "Обработка".
     */
    public void enableCancelProcess(boolean state) {
        buttonProcess.setText(state ? "Прервать" : "Обработка");
        cancelState = state;
    }

    /**
     * Установка текстового сообщения в статусной строке.
     * @param text Сообщение.
     */
    public void setProgressInfo(String text) {
        textInfo.setText(text);
    }

    /**
     * Установка текстового сообщения в прогрессе.
     * @param text Сообщение.
     */
    public void setProgressText(String text) {
        progressBar.setString(text);
        if (text == null) {
            progressBar.setString("");
        }
    }

    /**
     * Инициализация и запуск нового прогресса. Если нач.и кон.позиции = -1, то
     * стартует непрерывный процесс без показателя процента выполнения.
     * @param startpos Начальная позиция.
     * @param endpos Конечная позиция.
     */
    public void startProgress(int startpos, int endpos) {
        progressBar.setIndeterminate(startpos == -1 && endpos == -1);
        progressBar.setMinimum(startpos);
        progressBar.setMaximum(endpos);
        progressBar.setStringPainted(true);
        progressBar.setValue(startpos);
        progressBar.setString("");
    }

    /**
     * Инициализация и запуск нового непрерывного прогресса.
     */
    public void startProgress() {
        startProgress(-1, -1);
    }

    /**
     * Установка значения позиции для прогресса.
     * @param pos Позиция.
     */
    public void setProgress(int pos) {
        progressBar.setValue(pos);
    }

    /**
     * Остановка прогресса.
     */
    public void stopProgress() {
        progressBar.setIndeterminate(false);
    }

    @Override
    protected void processWindowEvent(WindowEvent e) {
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            /*
            Object[] options = {"Да", "Нет!"};
            int n = JOptionPane.showOptionDialog(e.getWindow(), "Выйти из программы?",
            "Подтверждение", JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE, null, options,
            options[0]);
            if (n == 0) {
            dispose();
            System.exit(0);
            }
             */
            System.exit(0);
        } else {
            super.processWindowEvent(e);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == buttonProcess) {
            if (!cancelState) {
                // Запуск задачи.
            } else {
                // Остановка задачи.
                App.mainFrame.enableProcess(false);
                if (!App.cancelTask()) {
                    App.mainFrame.enableProcess(true);
                }
            }
        }
    }
}
