package dvrextract;

import dvrextract.gui.GUI;
import dvrextract.gui.GUITabPane;
import dvrextract.gui.GUIFrame;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowEvent;
import javax.swing.*;
import net.miginfocom.swing.MigLayout;

/**
 * Основное окно приложения.
 * @author lex
 */
public final class GUI_Main extends GUIFrame {

    ////////////////////////////////////////////////////////////////////////////
    // Закладка: Источник
    ////////////////////////////////////////////////////////////////////////////
    public GUI_TabSource tabSource;
    ////////////////////////////////////////////////////////////////////////////
    // Закладка: Обработка
    ////////////////////////////////////////////////////////////////////////////
    JCheckBox checkRaw; // Создавать сырой файл?
    JTextField textRaw; // Путь и имя файла.
    JButton buttonRaw; // Выбор файла.
    //
    JCheckBox checkVideo; // Создавать видео?
    JTextField textVideo; // Путь и имя файла.
    JButton buttonVideo; // Выбор файла.
    JCheckBox checkSub; // Делать субтитры к видео?
    //
    JComboBox comboCams; // Выбор камеры из списка.
    JCheckBox checkAudio; // Обрабатывать аудио поток.
    ////////////////////////////////////////////////////////////////////////////
    // На всех вкладках
    ////////////////////////////////////////////////////////////////////////////
    JLabel labelInfo; // Строка состояния обработки.
    JButton buttonProcess; // Запуск\остановка обработки.
    JProgressBar progressBar; // Прогрес выполнения операции.

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

        progressBar = new JProgressBar();

        add(tabPane, BorderLayout.CENTER);

        JPanel panelButton = new JPanel(new MigLayout("", "[grow,fill][10px]"));
        JLabel lInfo = GUI.createLabel("Инфо:");
        panelButton.add(lInfo);
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
}
