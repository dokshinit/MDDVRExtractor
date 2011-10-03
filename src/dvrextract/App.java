package dvrextract;

import dvrextract.gui.GUI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.OceanTheme;

// TODO: Зависает обновление таблицы после долгого скролла.
// TODO: Хранить изображения?
// TODO: При выборе конкретной камеры при скане - не выводит список!
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

    /**
     * Вывод строки лога.
     * @param text Текст сообщения.
     */
    public static void log(String text) {
        System.out.println(text);
    }

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
    //
    static int camNumber = 0;
    static boolean isAudio = false;
    static String sInput = null;
    static String sFile = null;
    static String sVideo = null;
    static String sSRT = null;
    int frameCount = 0;
    int frameInStepCount = 0;
    //
    long startDataPos = 0;
    long endDataPos = 0;
    long curPos = 0;
    // Информация о источнике:
    // Подразумевается, что источником может быть или одиночный файл или
    // каталог. При этом каждый файл распознаётся исходя из имени файла:
    // по шалону *.exe - файл архива, по шаблону da*. - файл hdd.
    //
    // Каталог или файл.
    public static String srcName = "/home/work/files/";
    // Тип источника: 0-EXE, 1-HDD
    public static FileType srcType = FileType.NO;
    // Ограничение одной камерой (если = 0 - без ограничений).
    public static int srcCamLimit = 0;
    // Текущая выбранная камера для которой отображаются файлы.
    public static int srcCamSelect = 0;
    // Массив разделения источников по камерам.
    public static CamInfo[] srcCams = new CamInfo[MAXCAMS];

    /**
     * Точка запуска приложения.
     * @param Аргументы.
     */
    public static void main(String[] args) {

        for (int i = 0; i < MAXCAMS; i++) {
            srcCams[i] = new CamInfo();
        }

        initLAF();
        FFMpeg.init();

        // Старт многооконного приложения
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                // Позиционируем по центру экрана
                mainFrame = new GUI_Main();
                mainFrame.center();
                mainFrame.setVisible(true);
            }
        });

//        HDDFiles hdd = new HDDFiles("/home/work/files/AZSVIDEO/RESEARCH/rest/131");
//        HDDFiles hdd = new HDDFiles("/mnt/131");
//        hdd.scan(6);
//
//        for (int i = 0; i < App.MAXCAMS; i++) {
//            long size = 0, time = 0;
//            for (int n = 0; n < hdd.files[i].size(); n++) {
//                HDDFileInfo info = hdd.files[i].get(n);
//                size += info.fileSize;
//                time += info.frameLast.time.getTime() - info.frameFirst.time.getTime();
//            }
//            log("CAM" + (i + 1) + " files=" + hdd.files[i].size() + " size=" + size + " time=" + (time / 1000));
//        }


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


        /*
         * 1. Выбор источника данных: каталог или файл exe/hdd.
         * 2. Сканирование источника. 
         *    Если это файл, то параметры файла.
         *    Если каталог - параметры каждого файла в каталоге.
         * 3. Если каталог - выбор файлов.
         * 4. Настройка параметров обработки. 
         *    Видеопотока, титров, аудио, выходных файлов, доп.параметров ffmpeg.
         * 5. Обработка с отображением прогреса.
         */


    }
    //TODO: Два режима работы - графический и консольный.
    //TODO: Прикрутить ключи для консольного использования.
    //TODO: Сделать просмотр или просто первые кадры камер? Средства?
    //TODO: Сделать процедуру считывающую первый заголовок и последний и выдающую инфу наверх.
    //TODO: Сделать процедуру обрабатывающую все файлы в каталоге и собирающую инфу в разрезе камер.
    // к каждой камере - список файлов с определенными параметрами
    //Можно ли делать скриншоты из одного опорного кадра?
    //Каким образом в видео добавить дату-время (титрами?)?
}
