package dvrextract;

import dvrextract.FFMpeg.Cmd;
import dvrextract.gui.GUI;
import dvrextract.I10n.Lang;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.OceanTheme;

/**
 * Глобальный класс приложения.
 * @author Докшин Алексей Николаевич <dant.it@gmail.com>
 */
public class App {

    public static String x_LAFNotFound, x_LAFError, x_FFMpegWrong, x_CodecsWrong;
    //
    ////////////////////////////////////////////////////////////////////////////
    // Константы.
    ////////////////////////////////////////////////////////////////////////////
    // Версия программы.
    public static final String version = "0.9b3";
    // Максимальное кол-во обрабатываемых камер.
    public static final int MAXCAMS = 16;
    ////////////////////////////////////////////////////////////////////////////
    // Глобальные переменные.
    ////////////////////////////////////////////////////////////////////////////
    // Основное окно работы программы.
    public static GUI_Main gui;
    // Для отладки, если true - подробный лог.
    public static boolean isDebug = false;
    public static boolean isLinux = false;
    public static boolean isWindows = false;

    /**
     * Вывод строки лога с типом.
     * @param type Тип сообщения.
     * @param text Текст сообщения.
     */
    public static void log(LogTableModel.Type type, String text) {
        if (gui != null && gui.tabLog != null) {
            gui.tabLog.getLogPanel().add(type, text);
        }
        System.out.println(text);
    }

    /**
     * Обновление последней строки лога с типом.
     * @param text Текст сообщения.
     */
    public static void logupd(LogTableModel.Type type, String text) {
        if (gui != null && gui.tabLog != null) {
            gui.tabLog.getLogPanel().update(type, text);
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
            App.log(x_LAFNotFound + " [" + laf + "]! " + e);
        } catch (Exception e) {
            App.log(x_LAFError + " [" + laf + "]! " + e);
        }
    }
    ////////////////////////////////////////////////////////////////////////////
    // Информация о источнике:
    ////////////////////////////////////////////////////////////////////////////
    // Подразумевается, что источником может быть или одиночный файл или
    // каталог. При этом каждый файл распознаётся исходя из имени файла:
    // по шалону *.exe - файл архива, по шаблону da*. - файл hdd.
    //

    public static class Source {

        // Каталог или файл.
        private static String name = "/home/work/files/AZSVIDEO/1/1.exe";
        // Тип источника: 0-EXE, 1-HDD
        private static FileType type = FileType.NO;
        // Ограничение одной камерой (если = 0 - без ограничений).
        private static int limitedCam = 0;
        // Текущая выбранная камера для которой отображаются файлы.
        private static int selectedCam = 0;
        // Массив разделения источников по камерам.
        private static CamInfo[] cams = new CamInfo[MAXCAMS];

        /**
         * Возвращает путь\файл источника.
         * @return Путь\файл источника.
         */
        public static String getName() {
            return name;
        }

        /**
         * Возвращает тип источника.
         * @return Тип источника.
         */
        public static FileType getType() {
            return type;
        }

        /**
         * Возвращает номер камеры жёсткого оганичения при сканировании.
         * @return Номер камеры ограничения (0 - без ограничений).
         */
        public static int getLimitedCam() {
            return limitedCam;
        }

        /**
         * Возвращает номер выбранной камеры.
         * @return Номер выбранной камеры.
         */
        public static int getSelectedCam() {
            return selectedCam;
        }

        /**
         * Возвразает информацию для указанной камеры.
         * @param index Номер камеры (не индекс массива!): 1..MAXCAM
         * @return Информация для указанной камеры.
         */
        public static CamInfo getCamInfo(int index) {
            return cams[index - 1];
        }

        /**
         * Устанавливает новый источник.
         * @param newName Путь\имя файла источкика.
         * @param newType Тип источника.
         * @param limited Жесткое ограничение по камере (0 - без ограничений).
         */
        public static void set(String newName, FileType newType, int limited) {
            if (newName == null) {
                newName = "";
            }
            name = newName.trim();
            type = newType;
            limitedCam = limited;

            // Очистка всех данных о предыдущем сканировании.
            for (int i = 1; i <= MAXCAMS; i++) {
                Source.getCamInfo(i).clear();
            }
            // Обновление отображения.
            App.gui.tabSource.validateSourceChange();
            // Обновление блокировок.
            App.gui.validateLocks();
        }

        /**
         * Установка выбранной камеры.
         * @param newCam Номер камеры.
         */
        public static void setSelectedCam(int newCam) {
            if (selectedCam != newCam) {
                selectedCam = newCam;
                // Обновление блокировок.
                App.gui.validateLocks();
            }
        }
    }
    ////////////////////////////////////////////////////////////////////////////
    // Информация об обработке:
    ////////////////////////////////////////////////////////////////////////////

    public static class Dest {

        private static Date timeStart;
        private static Date timeEnd;
        private static String videoName = "";
        private static String audioName = "";
        private static String subName = "";
        private static Cmd videoOptions = new Cmd(false);
        private static Cmd audioOptions = new Cmd(false);
        private static Cmd subOptions = new Cmd(false);
        private static int audioType;
        private static int subType;

        public static Date getTimeStart() {
            return timeStart;
        }

        public static Date getTimeEnd() {
            return timeEnd;
        }

        public static String getVideoName() {
            return videoName;
        }

        public static String getAudioName() {
            return audioName;
        }

        public static String getSubName() {
            return subName;
        }

        public static Cmd getVideoOptions() {
            return videoOptions;
        }
        
        public static Cmd getAudioOptions() {
            return audioOptions;
        }

        public static Cmd getSubOptions() {
            return subOptions;
        }

        public static int getAudioType() {
            return audioType;
        }

        public static int getSubType() {
            return subType;
        }

        public static void setTimeStart(Date time) {
            timeStart = time;
            App.gui.validateLocks();
        }
        
        public static void setTimeEnd(Date time) {
            timeEnd = time;
            App.gui.validateLocks();
        }
        
        public static void setVideoName(String name) {
            videoName = name;
            setAudioName(Files.getNameWOExt(name) + ".wav");
            setSubName(Files.getNameWOExt(name) + ".srt");
            App.gui.validateLocks();
        }

        public static void setAudioName(String name) {
            audioName = name;
            App.gui.validateLocks();
        }

        public static void setSubName(String name) {
            subName = name;
            App.gui.validateLocks();
        }

        public static void setVideoOptions(Cmd opt) {
            videoOptions = opt;
            App.gui.validateLocks();
        }
        
        public static void setAudioOptions(Cmd opt) {
            audioOptions = opt;
            App.gui.validateLocks();
        }
        
        public static void setSubOptions(Cmd opt) {
            subOptions = opt;
            App.gui.validateLocks();
        }
        
        public static void setAudioType(int mode) {
            audioType = mode;
            App.gui.validateLocks();
        }
        
        public static void setSubType(int mode) {
            subType = mode;
            App.gui.validateLocks();
        }
        
    }
    //public static Date destTimeStart;
    //public static Date destTimeEnd;
//    public static String destVideoName = "";
//    public static String destAudioName = "";
//    public static String destSubName = "";
//    public static Cmd destVideoOptions = new Cmd(false);
//    public static Cmd destAudioOptions = new Cmd(false);
//    public static Cmd destSubOptions = new Cmd(false);
//    public static int destAudioType;
//    public static int destSubType;

    /**
     * Точка запуска приложения.
     * @param Аргументы.
     */
    public static void main(String[] args) {

        // Инициализация интернационализации.
        I10n.init(Lang.RU); // ru\en

        // Инициализация лога ошибок.
        Err.init();

        // Инициализация переменных.
        for (int i = 0; i < MAXCAMS; i++) {
            Source.cams[i] = new CamInfo();
        }
        // Определяем тип ОС.
        String s = System.getProperty("os.name").toLowerCase();
        if (s.substring(0, 3).equals("lin")) {
            isLinux = true;
        } else if (s.substring(0, 3).equals("win")) {
            isWindows = true;
        }

        // Инициализация Look&Feel.
        initLAF();

        // Инициализация FFMpeg (получение списков кодеков).
        FFMpeg.init();

        // Старт многооконного приложения
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                // Создание.
                GUI_Main.create();
                // Позиционируем по центру экрана.
                GUI.centerizeFrame(gui);
                gui.setVisible(true);
                // При проблемах с ffmpeg - сообщаем.
                if (!FFMpeg.isWorking()) {
                    JOptionPane.showMessageDialog(gui,
                            x_FFMpegWrong,
                            x_CodecsWrong,
                            JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        //TODO: Два режима работы - графический и консольный.
        //TODO: Прикрутить ключи для консольного использования.
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
