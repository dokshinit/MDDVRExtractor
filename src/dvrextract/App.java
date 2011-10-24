package dvrextract;

import dvrextract.gui.GUI;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.OceanTheme;

// TODO: Добавить определение операционной системы - давать соответствующий выбор сохранения результатов.
// TODO: Для линукса - запись видео\аудио\титров через три именованых канала в один файл, для винды - в три разных (два потока ffmpeg + файл srt).
// TODO: Проверка наличия нужных кодеков (g722 и srt)?
//
// TODO: Зависает обновление таблицы после долгого скролла.
/**
 *
 * @author lex
 */
public class App {

    ////////////////////////////////////////////////////////////////////////////
    // Константы.
    ////////////////////////////////////////////////////////////////////////////
    // Максимальное кол-во обрабатываемых камер.
    public static final int MAXCAMS = 16;
    ////////////////////////////////////////////////////////////////////////////
    // Глобальные переменные.
    ////////////////////////////////////////////////////////////////////////////
    public static GUI_Main mainFrame; // Основное окно работы программы.
    // Для отладки, если true - подробный лог.
    public static boolean isDebug = false;
    // Операционная система поддерживает именованные каналы (pipe).
    // Если да, то будет доступно в опциях - сливать всё в один файл!
    // Если нет, то только в два захода!
    public static boolean isPipe = false;
    
    /**
     * Вывод строки лога с типом.
     * @param type Тип сообщения.
     * @param text Текст сообщения.
     */
    public static void log(LogTableModel.Type type, String text) {
        if (mainFrame != null && mainFrame.tabLog != null) {
            mainFrame.tabLog.getLogPanel().add(type, text);
        }
        System.out.println(text);
    }

    /**
     * Обновление последней строки лога с типом.
     * @param text Текст сообщения.
     */
    public static void logupd(LogTableModel.Type type, String text) {
        if (mainFrame != null && mainFrame.tabLog != null) {
            mainFrame.tabLog.getLogPanel().update(type, text);
        }
        System.out.println(text);
    }

    /**
     * Вывод строки лога.
     * @param text Текст сообщения.
     */
    public static void log(String text) {
        log(LogTableModel.Type.TEXT, text);
    }

    /**
     * Обновление последней строки лога.
     * @param text Текст сообщения.
     */
    public static void logupd(String text) {
        logupd(LogTableModel.Type.TEXT, text);
    }

    /**
     * Инициализация Look&Feel.
     */
    public static void initLAF() {
        String laf = "javax.swing.plaf.metal.MetalLookAndFeel";
        //String laf = "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel";
        //String laf = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";

        // Отключение жирного шрифта в UI.
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        //UIManager.put("swing.useSystemFontSettings", Boolean.TRUE);

        // Отключение уродского градиента.
        List buttonGradient = Arrays.asList(
                new Object[]{new Float(1f), new Float(0f),
                    GUI.bgButton, GUI.bgButton, GUI.bgButton});
        List sliderGradient = Arrays.asList(new Object[]{});
        List menuGradient = Arrays.asList(new Object[]{});
        UIManager.put("Button.gradient", buttonGradient);
        UIManager.put("CheckBox.gradient", buttonGradient);
        UIManager.put("CheckBoxMenuItem.gradient", buttonGradient);
        UIManager.put("MenuBar.gradient", menuGradient);
        UIManager.put("RadioButton.gradient", buttonGradient);
        UIManager.put("RadioButtonMenuItem.gradient", buttonGradient);
        UIManager.put("ScrollBar.gradient", buttonGradient);
        UIManager.put("Slider.gradient", sliderGradient);
        UIManager.put("ToggleButton.gradient", buttonGradient);
        //
        //UIManager.put("ToggleButton.background", new Color(0xF8F8F8));
        //UIManager.put("ComboBox.background",  new Color(0xF8F8F8));
        //UIManager.put("ComboBox.buttonBackground", Color.BLUE);
        //UIManager.put("ComboBox.buttonDarkShadow", Color.red);
        //UIManager.put("ComboBox.buttonHighlight", Color.red);
        //UIManager.put("ComboBox.buttonShadow", Color.red);
        UIManager.put("ToolTip.background", GUI.colorToolTipBg);
        UIManager.put("ToolTip.foreground", GUI.colorToolTipFg);
        UIManager.put("ToolTip.border", GUI.borderToolTip);
        //JFrame.setDefaultLookAndFeelDecorated(true);
        //JDialog.setDefaultLookAndFeelDecorated(true);

        try {
            Class c = Class.forName(laf);
            UIManager.setLookAndFeel(laf);
            //MetalLookAndFeel.setCurrentTheme(new DefaultMetalTheme());
            MetalLookAndFeel.setCurrentTheme(new OceanTheme());
        } catch (java.lang.ClassNotFoundException e) {
            App.log("Не найден L&F (" + laf + ")! " + e);
        } catch (Exception e) {
            App.log("Ошибка включения L&F (" + laf + ")!" + e);
        }
    }
    ////////////////////////////////////////////////////////////////////////////
    // Информация о источнике:
    ////////////////////////////////////////////////////////////////////////////
    // Подразумевается, что источником может быть или одиночный файл или
    // каталог. При этом каждый файл распознаётся исходя из имени файла:
    // по шалону *.exe - файл архива, по шаблону da*. - файл hdd.
    //
    // Каталог или файл.
    public static String srcName = "/home/work/files/AZSVIDEO/1/1.exe";
    // Тип источника: 0-EXE, 1-HDD
    public static FileType srcType = FileType.NO;
    // Ограничение одной камерой (если = 0 - без ограничений).
    public static int srcCamLimit = 0;
    // Текущая выбранная камера для которой отображаются файлы.
    public static int srcCamSelect = 0;
    // Массив разделения источников по камерам.
    public static CamInfo[] srcCams = new CamInfo[MAXCAMS];
    ////////////////////////////////////////////////////////////////////////////
    // Информация об обработке:
    ////////////////////////////////////////////////////////////////////////////
    public static Date destTimeStart;
    public static Date destTimeEnd;
    public static String destVideoName = "";
    public static String destAudioName = "";
    public static String destSubName = "";
    public static String destVideoOptions = "";
    public static String destAudioOptions = "";
    public static String destSubOptions = "";
    public static int destAudioType;
    public static int destSubType;

    /**
     * Точка запуска приложения.
     * @param Аргументы.
     */
    public static void main(String[] args) {

        // Инициализация лога ошибок.
        Err.init();

        // Инициализация переменных.
        for (int i = 0; i < MAXCAMS; i++) {
            srcCams[i] = new CamInfo();
        }
        // Определяем можно ли пользоваться именованными каналами.
        // По умолчанию - нельзя.
        String s = System.getProperty("os.name").toLowerCase();
        if (s.substring(0, 3).equals("lin")) {
            isPipe = true;
        } else if (s.substring(0, 3).equals("win")) {
            isPipe = false;
        }

        // Инициализация Look&Feel.
        initLAF();

        // Инициализация FFMpeg (получение списков кодеков).
        FFMpeg.init();

        // Старт многооконного приложения
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                // Позиционируем по центру экрана
                mainFrame = new GUI_Main();
                mainFrame.center();
                mainFrame.setVisible(true);
                if (!FFMpeg.isWork()) {
                    JOptionPane.showMessageDialog(mainFrame,
                            "Некорректная работа FFMPEG!",
                            "Ошибка запроса кодеков",
                            JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        //TODO: Два режима работы - графический и консольный.
        //TODO: Прикрутить ключи для консольного использования.
        //TODO: Сделать просмотр или просто первые кадры камер? Средства?
        //TODO: Сделать процедуру считывающую первый заголовок и последний и выдающую инфу наверх.

        /*
         * Ориентировочный синтаксис использования из консоли:
         * 
         * info     Сбор и отображение информации о данных.
         *          Если источник hdd - собирает инфу о всех файлах.
         *          Если источник exe - выводит инфу о файле.
         * 
         * process  Обработка данных. Сначала делает шаг info, потом производит
         *          обработку данных для указанной камеры. Если не задано ни 
         *          одного действия по обработке, выполняется вхолостую с 
         *          выводом детальной инфы.
         * 
         * -src=<источник>
         *          Задание источника (файл - *.exe или daNNNNN \ каталог (hdd)).
         * 
         * ? -type=<тип>
         *          Задание типа данных источника. Если не задан - определяется
         *          исходя из названия файла (*.exe - archive, da* - hdd).
         *              hdd - каталог с файлами диска.
         *              archive - файл архивных данных.
         * 
         * -cam=<номер>
         *          Номер камеры.
         * 
         * -start=YYYY.MM.DD,HH:MM:SS
         *          Дата и время начала сохранения данных.
         * 
         * -end=YYYY.MM.DD,HH:MM:SS
         *          Дата и время конца сохранения данных.
         * 
         */
    }
}
