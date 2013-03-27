/*
 * Copyright (c) 2011-2012, Aleksey Nikolaevich Dokshin. All right reserved.
 * Contacts: dant.it@gmail.com, dokshin@list.ru.
 */
package mddvrextract;

import mddvrextract.FFMpeg.Cmd;
import mddvrextract.I18n.Lang;
import mddvrextract.gui.GUI;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import mddvrextract.xfsengine.Device;
import mddvrextract.xfsengine.XFS;
import mddvrextract.xfsengine.XFS.XFSException;

/**
 * Глобальный класс приложения.
 *
 * @author Докшин Алексей Николаевич <dant.it@gmail.com>
 */
public class App {

    /**
     * Дата релиза версии программы.
     */
    public static final String versionDate = "27.03.2013";
    /**
     * Версия программы.
     */
    public static final String version = "1.2.2";
    /**
     * Максимальное кол-во обрабатываемых камер.
     */
    public static final int MAXCAMS = 16;
    /**
     * Основное окно работы программы.
     */
    public static GUI_Main gui;
    /**
     * Для отладки, если true - подробный лог.
     */
    public static boolean isDebug = false;
    /**
     * Флаг запуска под Linux.
     */
    public static boolean isLinux = false;
    /**
     * Флаг запуска под Windows.
     */
    public static boolean isWindows = false;
    /**
     * Текстовые ресурсы для интерфейса.
     */
    public static String x_LAFNotFound, x_LAFError, x_FFMpegWrong, x_CodecsWrong;

    /**
     * Модель источника.
     *
     * Подразумевается, что источником может быть: одиночный файл, каталог,
     * устройство HDD-XFS. При этом для обычных файлов распознавание происходит
     * по имени файла: по шаблону *.exe - файл архива, по шаблону da*. - файл
     * hdd, а для HDD-XFS жестко задан шаблон da*.
     */
    public static class Source {

        /**
         * Полное имя файла / каталога / устройства с XFS.
         */
        private static FileDesc name = new FileDesc();
        /**
         * ФС XFS.
         */
        private static XFS xfs = null;
        /**
         * Тип источника: 0-EXE, 1-HDD.
         */
        private static FileType type = FileType.NO;
        /**
         * Ограничение одной камерой (если = 0 - без ограничений).
         */
        private static int limitedCam = 0;
        /**
         * Текущая выбранная камера для которой отображаются файлы.
         */
        private static int selectedCam = 0;
        /**
         * Массив разделения источников по камерам.
         */
        private static CamInfo[] cams = new CamInfo[MAXCAMS];

        /**
         * Возвращает путь\файл источника.
         *
         * @return Путь\файл источника.
         */
        public static FileDesc getName() {
            return name;
        }

        /**
         * Возвращает текущую XFS.
         *
         * @return XFS.
         */
        public static XFS getXFS() {
            return xfs;
        }

        /**
         * Открывает XFS для текущего устройства. Если уже открыта -
         * предварительно закрывает старую.
         *
         * @return XFS.
         * @throws FileNotFoundException Файл устройства не найден.
         * @throws IOException Ошибка ввода-вывода.
         * @throws XFSException Ошибка структуры XFS.
         */
        public static XFS openXFS() throws FileNotFoundException, IOException, XFSException {
            if (xfs != null) {
                closeXFS();
            }
            return xfs = new XFS(new Device(name.name));
        }

        /**
         * Закрывает (если открыта) текущую XFS и связанное устройство.
         */
        public static void closeXFS() {
            if (xfs != null) {
                try {
                    Device dev = xfs.device;
                    xfs.close();
                    dev.close();
                } catch (IOException ex) {
                }
                xfs = null;
            }
        }

        /**
         * Возвращает тип источника.
         *
         * @return Тип источника.
         */
        public static FileType getType() {
            return type;
        }

        /**
         * Возвращает номер камеры жёсткого оганичения при сканировании.
         *
         * @return Номер камеры ограничения (0 - без ограничений).
         */
        public static int getLimitedCam() {
            return limitedCam;
        }

        /**
         * Возвращает номер выбранной камеры.
         *
         * @return Номер выбранной камеры.
         */
        public static int getSelectedCam() {
            return selectedCam;
        }

        /**
         * Возвразает информацию для указанной камеры.
         *
         * @param index Номер камеры (не индекс массива!): 1..MAXCAM
         * @return Информация для указанной камеры.
         */
        public static CamInfo getCamInfo(int index) {
            return cams[index - 1];
        }

        /**
         * Устанавливает новый источник.
         *
         * @param newName Путь\имя файла источкика.
         * @param newType Тип источника.
         * @param limited Жесткое ограничение по камере (0 - без ограничений).
         */
        public static void set(FileDesc newName, FileType newType, int limited) {
            if (newName == null) {
                newName = new FileDesc();
            }
            name = new FileDesc(newName);
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
         *
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

    /**
     * Модель приёмника.
     */
    public static class Dest {

        /**
         * Дата начала сохраняемого периода.
         */
        private static Date timeStart;
        /**
         * Дата конца сохраняемого периода.
         */
        private static Date timeEnd;
        /**
         * Полное имя файла-приёмника видео.
         */
        private static String videoName = "";
        /**
         * Полное имя файла-приёмника аудио.
         */
        private static String audioName = "";
        /**
         * Полное имя файла-приёмника субтитров.
         */
        private static String subName = "";
        /**
         * Опции ffmpeg для видео.
         */
        private static Cmd videoOptions = new Cmd(false);
        /**
         * Опции ffmpeg для аудио.
         */
        private static Cmd audioOptions = new Cmd(false);
        /**
         * Опции ffmpeg для субтитров.
         */
        private static Cmd subOptions = new Cmd(false);
        /**
         * Режим сохранения аудио: -1-не сохранять, 0-в файл аудио, 1-в файл
         * видео.
         */
        private static int audioType;
        /**
         * Режим сохранения субтитров: -1-не сохранять, 0-в файл субтитров, 1-в
         * файл видео.
         */
        private static int subType;

        /**
         * Возвращает дату начала сохраняемого периода.
         *
         * @return Дата.
         */
        public static Date getTimeStart() {
            return timeStart;
        }

        /**
         * Возвращает дату конца сохраняемого периода.
         *
         * @return Дата.
         */
        public static Date getTimeEnd() {
            return timeEnd;
        }

        /**
         * Возвращает имя файла-приёмника видео.
         *
         * @return Имя файла.
         */
        public static String getVideoName() {
            return videoName;
        }

        /**
         * Возвращает имя файла-приёмника аудио.
         *
         * @return Имя файла.
         */
        public static String getAudioName() {
            return audioName;
        }

        /**
         * Возвращает имя файла-приёмника субтитров.
         *
         * @return Имя файла.
         */
        public static String getSubName() {
            return subName;
        }

        /**
         * Возвращает опции ffmpeg для видео.
         *
         * @return Опции.
         */
        public static Cmd getVideoOptions() {
            return videoOptions;
        }

        /**
         * Возвращает опции ffmpeg для аудио.
         *
         * @return Опции.
         */
        public static Cmd getAudioOptions() {
            return audioOptions;
        }

        /**
         * Возвращает опции ffmpeg для субтитров.
         *
         * @return Опции.
         */
        public static Cmd getSubOptions() {
            return subOptions;
        }

        /**
         * Возвращает тип сохранения аудио.
         *
         * @return Тип: -1 - не сохранять, 0 - в файл аудио, 1 - в файл видео.
         */
        public static int getAudioType() {
            return audioType;
        }

        /**
         * Возвращает тип сохранения субтитров.
         *
         * @return Тип: -1 - не сохранять, 0 - в файл субтитров, 1 - в файл
         * видео.
         */
        public static int getSubType() {
            return subType;
        }

        /**
         * Устанавливает дату начала сохраняемого периода.
         *
         * @param time Дата
         */
        public static void setTimeStart(Date time) {
            timeStart = time;
            App.gui.validateLocks();
        }

        /**
         * Устанавливает дату конца сохраняемого периода.
         *
         * @param time Дата
         */
        public static void setTimeEnd(Date time) {
            timeEnd = time;
            App.gui.validateLocks();
        }

        /**
         * Устанавливает имя файла-приёмника для видео.
         *
         * @param name Имя файла.
         */
        public static void setVideoName(String name) {
            videoName = name;
            setAudioName(Files.getNameWOExt(name) + ".wav");
            setSubName(Files.getNameWOExt(name) + ".srt");
            App.gui.validateLocks();
        }

        /**
         * Устанавливает имя файла-приёмника для аудио.
         *
         * @param name Имя файла.
         */
        public static void setAudioName(String name) {
            audioName = name;
            App.gui.validateLocks();
        }

        /**
         * Устанавливает имя файла-приёмника для субтитров.
         *
         * @param name Имя файла.
         */
        public static void setSubName(String name) {
            subName = name;
            App.gui.validateLocks();
        }

        /**
         * Устанавливает опции ffmpeg для видео.
         *
         * @param opt Опции.
         */
        public static void setVideoOptions(Cmd opt) {
            videoOptions = opt;
            App.gui.validateLocks();
        }

        /**
         * Устанавливает опции ffmpeg для аудио.
         *
         * @param opt Опции.
         */
        public static void setAudioOptions(Cmd opt) {
            audioOptions = opt;
            App.gui.validateLocks();
        }

        /**
         * Устанавливает опции ffmpeg для субтитров.
         *
         * @param opt Опции.
         */
        public static void setSubOptions(Cmd opt) {
            subOptions = opt;
            App.gui.validateLocks();
        }

        /**
         * Устанавливает тип сохранения аудио.
         *
         * @param mode Тип: -1 - не сохранять, 0 - в файл аудио, 1 - в файл
         * видео.
         */
        public static void setAudioType(int mode) {
            audioType = mode;
            App.gui.validateLocks();
        }

        /**
         * Устанавливает тип сохранения субтитров.
         *
         * @param mode Тип: -1 - не сохранять, 0 - в файл аудио, 1 - в файл
         * видео.
         */
        public static void setSubType(int mode) {
            subType = mode;
            App.gui.validateLocks();
        }
    }

    /**
     * Вывод строки лога с типом.
     *
     * @param type Тип сообщения.
     * @param text Текст сообщения.
     */
    public static void log(LogTableModel.Type type, String text) {
        if (gui != null && gui.tabLog != null) {
            gui.tabLog.getLogPanel().add(type, text);
        }
    }

    /**
     * Обновление последней строки лога с типом.
     *
     * @param type Тип сообщения.
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
     *
     * @param text Текст сообщения.
     */
    public static void log(String text) {
        log(LogTableModel.Type.TEXT, text);
    }

    /**
     * Обновление последней строки лога.
     *
     * @param text Текст сообщения.
     */
    public static void logupd(String text) {
        logupd(LogTableModel.Type.TEXT, text);
    }

    /**
     * Инициализация Look&Feel.
     */
    public static void initLAF() {
        //String laf = "javax.swing.plaf.metal.MetalLookAndFeel";
        String laf = "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel";
        // Отключение жирного шрифта в UI.
        UIManager.put("swing.boldMetal", Boolean.FALSE);

        try {
            Class c = Class.forName(laf);
            UIManager.setLookAndFeel(laf);
        } catch (java.lang.ClassNotFoundException e) {
            App.log(x_LAFNotFound + " [" + laf + "]! " + e);
        } catch (Exception e) {
            App.log(x_LAFError + " [" + laf + "]! " + e);
        }

        GUI.init();
    }

    /**
     * Точка запуска приложения.
     *
     * @param args Аргументы.
     */
    public static void main(String[] args) {

        // Инициализация интернационализации.
        I18n.init(Lang.RU); // ru\en

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
        GUI.InSwingLater(new Runnable() {

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
    }
}
