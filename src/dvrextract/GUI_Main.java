/*
 * Copyright (c) 2011, Aleksey Nikolaevich Dokshin. All right reserved.
 * Contacts: dant.it@gmail.com, dokshin@list.ru.
 */
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
 * @author Докшин Алексей Николаевич <dant.it@gmail.com>
 */
public final class GUI_Main extends GUIFrame implements ActionListener {

    // Строки привязанные к языку.
    public static String x_Confirmation, x_ExitQuest, x_Help, x_Info, x_Log,
            x_No, x_Process, x_Source, x_State, x_Yes, x_Interrupt;
    //
    ////////////////////////////////////////////////////////////////////////////
    // На всех вкладках
    private JTextField textInfo; // Строка состояния обработки.
    private JButton buttonProcess; // Запуск\остановка обработки.
    private JProgressBar progressBar; // Прогрес выполнения операции.
    ////////////////////////////////////////////////////////////////////////////
    public GUI_TabSource tabSource;
    public GUI_TabProcess tabProcess;
    public GUI_TabLog tabLog;
    public GUI_TabAbout tabAbout;
    // Сосотояние кнопки "Обработка".
    // Если true - "Отмена" (остановка текущей задачи), false - "Обработка" (запуск обработки).
    private boolean cancelState = true;

    /**
     * Создание окна.
     */
    public static void create() {
        if (App.gui == null) {
            new GUI_Main();
        }
    }

    /**
     * Конструктор. Создание постоянных компонентов.
     */
    private GUI_Main() {
        // Первоначально инициализируем переменную, т.к. её используют почти все
        // компонеты, причем в том числе и на этапе инициализации.
        App.gui = GUI_Main.this;

        buttonProcess = GUI.createButton(x_Process);
        progressBar = new JProgressBar();
        textInfo = GUI.createText(100);
        //
        tabSource = new GUI_TabSource();
        tabProcess = new GUI_TabProcess();
        tabLog = new GUI_TabLog();
        tabAbout = new GUI_TabAbout();

        createUI();
    }

    /**
     * Построение пользовательского интерфейса.
     */
    public void createUI() {
        setTitle("DVR Extractor v" + App.version + " (ознакомительная версия)");
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setPreferredSize(new Dimension(890, 640));

        GUITabPane tabPane = new GUITabPane();

        // Вкладка "Источник"
        tabPane.addTab(x_Source, tabSource);

        // Вкладка "Обработка"
        tabPane.addTab(x_Process, tabProcess);

        // Вкладка "Состояние" (обработки - для реализации позже)
        JPanel panelTab3 = new JPanel(new MigLayout());
        tabPane.addTab(x_State, panelTab3);
        tabPane.setEnable(panelTab3, false);

        // Вкладка "Лог"
        tabPane.addTab(x_Log, tabLog);

        // Вкладка "О программе"
        tabPane.addTab(x_Help, tabAbout);

        buttonProcess.addActionListener(this);
        progressBar.setPreferredSize(new Dimension(100, 20));

        add(tabPane, BorderLayout.CENTER);

        JPanel panelButton = new JPanel(new MigLayout("", "[][grow,fill][10px]"));

        panelButton.add(GUI.createLabel(x_Info));
        panelButton.add(textInfo, "");
        textInfo.setEditable(false);
        textInfo.setBackground(new Color(0xE0E0E0));
        panelButton.add(buttonProcess, "spany 2, growy, wrap");
        panelButton.add(progressBar, "spanx 2, growx");
        add(panelButton, BorderLayout.SOUTH);

        // Инициируем построение UI закладок.
        tabSource.createUI();
        tabProcess.createUI();
        tabLog.createUI();
        tabAbout.createUI();

        pack();

        // Обновляем состояния всех элементов исходя из условий.
        validateLocks();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == buttonProcess) {
            if (!Task.isAlive()) {
                // Запуск задачи.
                Task.start(new ProcessTask());
            } else {
                // Остановка задачи.
                Task.terminate();
            }
        }
    }

    /**
     * Определение возможности запуска обработки.
     * @return Флаг состояния: true - можно, false - нельзя.
     */
    private boolean isPossibleProcess() {
        // ffmpeg не найден.
        if (!FFMpeg.isWorking()) {
            return false;
        }
        // Не выбрана камера.
        int cam = App.Source.getSelectedCam();
        if (cam <= 0) {
            return false;
        }
        // Нет файлов для обработки (не должно быть по идее).
        if (!App.Source.getCamInfo(cam).isExists) {
            return false;
        }
        // Не выбран выходной файл видео.
        if (App.Dest.getVideoName().isEmpty()) {
            return false;
        }
        // Если выбрано сохранение в файл аудио, проверяем выбран ли файл.
        if (App.Dest.getAudioType() >= 0 && App.Dest.getAudioName().isEmpty()) {
            return false;
        }
        // Если выбрано сохранение в файл субтитров, проверяем выбран ли файл.
        if (App.Dest.getSubType() >= 0 && App.Dest.getSubName().isEmpty()) {
            return false;
        }
        return true;
    }

    /**
     * Выставление блокировок всех элементов согласно текущему состоянию - 
     * отложенный запуск.
     */
    public void validateLocks() {
        GUI.InSwingLater(new Runnable() {

            @Override
            public void run() {
                if (Task.isAlive()) {
                    // Выполняется задача.
                    buttonProcess.setEnabled(true);
                    buttonProcess.setText(x_Interrupt);
                    cancelState = true;
                } else {
                    // Задач нет.
                    buttonProcess.setEnabled(isPossibleProcess());
                    buttonProcess.setText(x_Process);
                    cancelState = false;
                }
                tabSource.validateLocks();
                tabProcess.setLocks();
            }
        });
    }

    /**
     * Установка текстового сообщения в статусной строке.
     * @param text Сообщение.
     */
    public void setProgressInfo(final String text) {
        GUI.InSwingLater(new Runnable() {

            @Override
            public void run() {
                textInfo.setText(text);
            }
        });
    }

    /**
     * Установка текстового сообщения в прогрессе.
     * @param text Сообщение.
     */
    public void setProgressText(final String text) {
        GUI.InSwingLater(new Runnable() {

            @Override
            public void run() {
                progressBar.setString(text);
                if (text == null) {
                    progressBar.setString("");
                }
            }
        });
    }

    /**
     * Инициализация и запуск нового прогресса. Если нач.и кон.позиции = -1, то
     * стартует непрерывный процесс без показателя процента выполнения.
     * @param startpos Начальная позиция.
     * @param endpos Конечная позиция.
     */
    public void startProgress(final int startpos, final int endpos) {
        GUI.InSwingLater(new Runnable() {

            @Override
            public void run() {
                progressBar.setIndeterminate(startpos == -1 && endpos == -1);
                progressBar.setMinimum(startpos);
                progressBar.setMaximum(endpos);
                progressBar.setStringPainted(true);
                progressBar.setValue(startpos);
                progressBar.setString("");
            }
        });
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
    public void setProgress(final int pos) {
        GUI.InSwingLater(new Runnable() {

            @Override
            public void run() {
                progressBar.setValue(pos);
            }
        });
    }

    /**
     * Остановка прогресса.
     */
    public void stopProgress() {
        GUI.InSwingLater(new Runnable() {

            @Override
            public void run() {
                progressBar.setIndeterminate(false);
                progressBar.setMinimum(0);
                progressBar.setMaximum(0);
                progressBar.setValue(0);
                progressBar.setString("");
            }
        });
    }

    /**
     * Обработка событий окна для отлова попытки закрытия окна.
     * @param e Событие окна.
     */
    @Override
    protected void processWindowEvent(WindowEvent e) {
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {

            Object[] options = {x_Yes, x_No};
            int n = JOptionPane.showOptionDialog(e.getWindow(), x_ExitQuest,
                    x_Confirmation, JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE, null, options,
                    options[1]);
            if (n == 0) {
                dispose();
                System.exit(0);
            }

        } else {
            super.processWindowEvent(e);
        }
    }

    /**
     * Процесс обработки данных.
     */
    private class ProcessTask extends Task.Thread {

        @Override
        protected void task() {
            DataProcessor.process();
        }
    }
}
