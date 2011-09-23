package dvrextract;

import dvrextract.gui.GUI;
import dvrextract.gui.GUITabPane;
import dvrextract.gui.GUIFrame;
import java.awt.BorderLayout;
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
    private JLabel labelInfo; // Строка состояния обработки.
    private JButton buttonProcess; // Запуск\остановка обработки.
    private JProgressBar progressBar; // Прогрес выполнения операции.
    private JProgressBar progressBarTask; // Прогрес выполнения задачи.

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
        JPanel ptabProcess = new JPanel(new MigLayout());
        tabPane.addTab("Обработка", ptabProcess);

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

        //textInput = new JTextField(50);
        //buttonInput = new JButton("Выбор");


        textVideo = new JTextField(50);
        buttonVideo = GUI.createButton("Выбор");

        comboCams = new JComboBox();
        checkAudio = new JCheckBox("Включать аудиоданные.");

        buttonProcess = GUI.createButton("Обработка");
        buttonProcess.addActionListener(this);

        progressBar = new JProgressBar();
        progressBar.setPreferredSize(new Dimension(100, 20));

        add(tabPane, BorderLayout.CENTER);

        JPanel panelButton = new JPanel(new MigLayout("", "[grow,fill][10px]"));
        
        panelButton.add(labelInfo = GUI.createLabel("Инфо: "));
        panelButton.add(buttonProcess, "spany 2, growy, wrap");
        panelButton.add(progressBar);
        add(panelButton, BorderLayout.SOUTH);
        //panelButton.setBackground(Color.red);

        pack();
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
    
    private boolean cancelState = true;
    
    /**
     * Разрешение/запрет запуска обработки.
     * @param state Статус разрешения.
     */
    public void enableProcess(boolean state) {
        buttonProcess.setEnabled(state);
    }

    public void enableCancelProcess(boolean state) {
        buttonProcess.setText(state ? "Прервать" : "Обработка");
        cancelState = state;
    }

    public void setProgressInfo(String text) {
        labelInfo.setText(text);
    }
    
    public void setProgressText(String text) {
        progressBar.setString(text);
        if (text == null) {
            progressBar.setString("");
        }
    }
    
    public void startProgress(int startpos, int endpos) {
        progressBar.setIndeterminate(startpos == -1 && endpos == -1);
        progressBar.setMinimum(startpos);
        progressBar.setMaximum(endpos);
        progressBar.setStringPainted(true);
        progressBar.setValue(startpos);
        progressBar.setString("");
    }
    
    public void startProgress() {
        startProgress(-1, -1);
    }
    
    public void setProgress(int pos) {
        progressBar.setValue(pos);
    }
    
    public void stopProgress() {
        progressBar.setIndeterminate(false);
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
