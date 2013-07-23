/*
 * Copyright (c) 2011-2012, Aleksey Nikolaevich Dokshin. All right reserved.
 * Contacts: dant.it@gmail.com, dokshin@list.ru.
 */
package mddvrextract;

import mddvrextract.I18n.Lang;
import mddvrextract.gui.GUI;
import mddvrextract.gui.GUIFrame;
import mddvrextract.gui.GUITabPane;
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
 *
 * @author Докшин Алексей Николаевич <dant.it@gmail.com>
 */
public final class GUI_Main extends GUIFrame implements ActionListener {

    ////////////////////////////////////////////////////////////////////////////
    // На всех вкладках.
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Надпись "Инфо".
     */
    private JLabel labelInfo;
    /**
     * Строка состояния обработки.
     */
    private JTextField textInfo;
    /**
     * Запуск\остановка обработки.
     */
    private JButton buttonProcess;
    /**
     * Прогрес выполнения операции.
     */
    private JProgressBar progressBar;
    /**
     * Список языков интерфейса.
     */
    private JComboBox comboLang;
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Табулятор.
     */
    private GUITabPane tabPane;
    /**
     * Закладка "Источник".
     */
    public GUI_TabSource tabSource;
    /**
     * Закладка "Обработка".
     */
    public GUI_TabProcess tabProcess;
    /**
     * Закладка "Лог".
     */
    public GUI_TabLog tabLog;
    /**
     * Закладка "Справка".
     */
    public GUI_TabAbout tabAbout;
    /**
     * Сосотояние кнопки "Обработка". Если true - "Отмена" (остановка текущей
     * задачи), false - "Обработка" (запуск обработки).
     */
    private boolean cancelState = true;
    /**
     * Текстовые ресурсы для интерфейса.
     */
    public static String x_TabSource, x_TabProcess, x_TabState, x_TabLog, x_TabHelp,
            x_LabelInfo, x_ExitQuest, x_TitleSuffix, x_ButtonProcess, x_ButtonInterrupt;

    /**
     * Создание окна.
     */
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
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

        comboLang = new JComboBox(Lang.values());
        comboLang.addActionListener(GUI_Main.this);
        buttonProcess = GUI.createButton(x_ButtonProcess);
        buttonProcess.addActionListener(GUI_Main.this);
        progressBar = new JProgressBar();
        textInfo = GUI.createText(100);

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
        setTitle("MDDVR Extractor v" + App.version + " " + x_TitleSuffix);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setPreferredSize(new Dimension(920, 660));
        setMinimumSize(new Dimension(500, 400));

        tabPane = new GUITabPane();

        // Вкладка "Источник"
        tabPane.addTab(x_TabSource, tabSource);

        // Вкладка "Обработка"
        tabPane.addTab(x_TabProcess, tabProcess);

//        // Вкладка "Состояние" (обработки - для реализации позже)
//        JPanel panelTab3 = new JPanel(new MigLayout());
//        tabPane.addTab(x_TabState, panelTab3);
//        //tabPane.setEnable(panelTab3, false);
//        tabPane.setEnabledAt(2, false);

        // Вкладка "Лог"
        tabPane.addTab(x_TabLog, tabLog);

        // Вкладка "О программе"
        tabPane.addTab(x_TabHelp, tabAbout);

        JPanel p = tabPane.getBarPanel();
        p.add(new JLabel(), "push");
        p.add(comboLang);

        progressBar.setPreferredSize(new Dimension(100, 20));

        add(tabPane, BorderLayout.CENTER);

        JPanel panelButton = new JPanel(new MigLayout("", "[][grow,fill][10px]"));
        panelButton.add(labelInfo = GUI.createLabel(x_LabelInfo));
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

    /**
     * Актуализация контента при смене языка отображения.
     */
    public void updateLocale() {

        setTitle("MDDVR Extractor v" + App.version + " " + x_TitleSuffix);

        tabPane.setTitleAt(0, x_TabSource);
        tabPane.setTitleAt(1, x_TabProcess);
        //tabPane.setTitleAt(2, x_TabState);
        tabPane.setTitleAt(2, x_TabLog);
        tabPane.setTitleAt(3, x_TabHelp);

        labelInfo.setText(x_LabelInfo);
        textInfo.setText("");

        tabSource.updateLocale();
        tabProcess.updateLocale();
        tabLog.updateLocale();
        tabAbout.updateLocale();

        // Обновляем состояния всех элементов исходя из условий.
        validateLocks(); // Обновление надписи на кнопке Обработка согласно состоянию.

        validate();
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
        } else if (e.getSource() == comboLang) {
            I18n.init((Lang) comboLang.getSelectedItem());
            //createUI();
            updateLocale();
        }
    }

    /**
     * Определение возможности запуска обработки.
     *
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
                    comboLang.setEnabled(false);
                    buttonProcess.setEnabled(true);
                    buttonProcess.setText(x_ButtonInterrupt);
                    cancelState = true;
                } else {
                    // Задач нет.
                    comboLang.setEnabled(true);
                    buttonProcess.setEnabled(isPossibleProcess());
                    buttonProcess.setText(x_ButtonProcess);
                    cancelState = false;
                }
                if (tabSource != null) {
                    tabSource.validateLocks();
                }
                if (tabProcess != null) {
                    tabProcess.validateLocks();
                }
            }
        });
    }

    /**
     * Установка текстового сообщения в статусной строке.
     *
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
     *
     * @param text Сообщение.
     */
    public void setProgressText(final String text) {
        GUI.InSwingLater(new Runnable() {
            @Override
            public void run() {
                progressBar.setString(text);
//                if (text == null) {
//                    progressBar.setString("");
//                }
            }
        });
    }

    /**
     * Инициализация и запуск нового прогресса.
     *
     * Если нач.и кон.позиции = -1, то стартует непрерывный процесс без
     * показателя процента выполнения.
     *
     * @param startpos Начальная позиция.
     * @param endpos Конечная позиция.
     */
    public void startProgress(final int startpos, final int endpos) {
        GUI.InSwingLater(new Runnable() {
            @Override
            public void run() {
                boolean isInd = startpos == -1 && endpos == -1;
                progressBar.setIndeterminate(isInd);
                progressBar.setMinimum(isInd ? 0 : startpos);
                progressBar.setMaximum(isInd ? 100 : endpos);
                progressBar.setStringPainted(true);
                progressBar.setValue(isInd ? 0 : startpos);
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
     *
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
                progressBar.setMaximum(100);
                progressBar.setValue(0);
                progressBar.setString("");
            }
        });
    }

    /**
     * Обработка событий окна для отлова попытки закрытия окна.
     *
     * @param e Событие окна.
     */
    @Override
    protected void processWindowEvent(WindowEvent e) {
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            if (App.showConfirmDialog(x_ExitQuest)) {
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
