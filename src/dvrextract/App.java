/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dvrextract;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.UIManager;

/**
 *
 * @author lex
 */
public class App {

    public static final int MAXCAMS = 8; // Максимальное кол-во обрабатываемых камер.

    // Типы обрабатываемых файлов
    public enum FileType {

        EXE, HDD
    };

    /**
     * Вывод строки лога.
     * @param text Текст сообщения.
     */
    public static void log(String text) {
        System.out.println(text);
    }
    public static MainFrame mainFrame;
    static int camNumber = 0;
    static boolean isAudio = false;
    static String sInput = null;
    static String sFile = null;
    static String sVideo = null;
    static String sSRT = null;
    int frameCount = 0;
    int frameInStepCount = 0;
    //

    class CamInfo {

        boolean isExists; // для архива - если есть флаг, для hdd - если есть файлы
        long framesCount; // только для архива
        long framesSize; // только для архива
        Date minTime, maxTime;
        ArrayList<HDDFileInfo> files;

        public CamInfo() {
            isExists = false;
            framesCount = 0;
            framesSize = 0;
            minTime = null;
            maxTime = null;
        }
    }
    CamInfo[] info = new CamInfo[MAXCAMS];
    long startDataPos = 0;
    long endDataPos = 0;
    long curPos = 0;

//    boolean parseFrame() {
//        try {
//            readIn(baHeader, baHeader.length);
//            curPos += baHeader.length;
//
//            bbH.position(4);
//            // Дата-время (смещение в секундах от 1970 г.)
//            cf_Time = getDate(bbH.getInt());
//            // Номер камеры (+допинфа в старшем байте? игнорируем).
//            cf_CamNumber = (bbH.getInt() & 0xFF) + 1;
//            if (cf_CamNumber < 1 || cf_CamNumber > MAXCAMS) {
//                log("Возможное нарушение целостности данных! "
//                        + "Номер камеры вне диапазона [1..32] = " + cf_CamNumber);
//                return false;
//            }
//            if (!info[cf_CamNumber - 1].isExists) {
//                log("Возможное нарушение целостности данных! "
//                        + "Номер камеры вне списка записанных камер = " + cf_CamNumber);
//                return false;
//            }
//            incPos(7);
//            cf_isMainFrame = bbH.get() == 0 ? true : false; // Базовый кадр.
//            incPos(5);
//            cf_VideoSize = bbH.getInt(); // Размер кадра видеоданных.
//            cf_AudioSize = bbH.getInt(); // Размер кадра аудиоданных.
//            incPos(12);
//            cf_Number = bbH.getInt(); // Номер кадра.
//            //incPos(16);
//            //incPos(16); // Название камеры 16 байт - пока не требуется.
//            //incPos(24);
//            //log("Cam=" + cam + " Frame=" + number + " SizeV=" + sizev + " SizeA=" + sizea);
//
//            curPos += cf_VideoSize + cf_AudioSize;
//            if (cf_CamNumber == camNumber && (fout != null || procOut != null)) {
//                if (cf_VideoSize + cf_AudioSize > baFrame.length) {
//                    log("Размер фрейма превышает размер буфера! = " + (cf_VideoSize + cf_AudioSize));
//                    return false;
//                }
//                readIn(baFrame, cf_VideoSize + cf_AudioSize);
//                if (isAudio) {
//                    if (fout != null) {
//                        fout.write(baFrame, 0, cf_VideoSize + cf_AudioSize);
//                    }
//                    if (procOut != null) {
//                        procOut.write(baFrame, 0, cf_VideoSize + cf_AudioSize);
//                    }
//                } else {
//                    if (fout != null) {
//                        fout.write(baFrame, 0, cf_VideoSize);
//                    }
//                    if (procOut != null) {
//                        procOut.write(baFrame, 0, cf_VideoSize);
//                    }
//                }
//
//            } else {
//                skipIn(cf_VideoSize + cf_AudioSize);
//            }
//
//            frameCount++;
//            frameInStepCount++;
//            if (frameInStepCount >= 5000) {
//                frameInStepCount = 0;
//                long progress = 100L * (curPos - startDataPos) / (endDataPos - startDataPos);
//                log("Frame=" + cf_Number + " [" + progress + "%]");
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//            return false;
//        }
//        return true;
//    }
//
//    int parse() {
//        try {
//            File f = new File(sInput);
//            if (!f.exists()) {
//                log("Файл-источник не существует! " + sInput);
//                return 1;
//            }
//            if (!f.canRead()) {
//                log("Файл-источник не доступен для чтения! " + sInput);
//                return 2;
//            }
//
//            ////////////////////////////////////////////////////////////////////
//            // Считываем информацию о данных.
//            ////////////////////////////////////////////////////////////////////
//            in = new RandomAccessFile(f, "r");
//            in.seek(in.length() - 28 * 16);
//            in.readFully(baFrame, 0, 28 * 16);
//
//            // Наличие треков камер.
//            for (int i = 0; i < MAXCAMS; i++) {
//                info[i] = new CamInfo();
//                info[i].isExists = bbF.getInt() != 0 ? true : false;
//                info[i].framesCount = 0;
//                info[i].framesSize = 0;
//                if (info[i].isExists) {
//                    log("CAM" + (i + 1) + " данные в наличии!");
//                }
//            }
//            // Позиции начала и конца данных в файле.
//            startDataPos = bbF.getInt();
//            endDataPos = bbF.getInt(12 * 16);
//            log("Общий размер данных = " + (endDataPos - startDataPos));
//
//
//            ////////////////////////////////////////////////////////////////////
//            // Считываем данные.
//            ////////////////////////////////////////////////////////////////////
//            if (info[camNumber - 1].isExists) {
//                log("CAM" + camNumber + " обработка...");
//
//                if (sVideo != null) {
//                    proc = Runtime.getRuntime().exec(
//                            new String[]{"ffmpeg", "-vcodec", "copy", "-r", "12.5",
//                                "-i", "-", sVideo});
//                    procOut = proc.getOutputStream();
//                }
//
//                if (sFile != null) {
//                    fout = new FileOutputStream(sFile);
//                }
//
//                in.seek(startDataPos);
//                curPos = startDataPos;
//
//                while (curPos < endDataPos) {
//                    if (!parseFrame()) {
//                        break;
//                    }
//                }
//
//                if (fout != null) {
//                    fout.flush();
//                    fout.close();
//                }
//
//                if (procOut != null) {
//                    procOut.flush();
//                    procOut.close();
//                    try {
//                        proc.exitValue();
//                    } catch (IllegalThreadStateException ee) {
//                        log("Ожидание завершения обработки ffmpeg...");
//                        proc.waitFor();
//                        log("... обработка ffmpeg завершена.");
//                    }
//                }
//
//            } else {
//                log("Файл-источник не содержит данных для CAM" + camNumber + ".");
//                return 3;
//            }
//            in.close();
//
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            return -1; // Прочие ошибки исполнения.
//        }
//
//        return 0;
//    }
//    //
//    Date srtTime = null;
//    long srtFrame = 0;
//    long srtFrameCount = 0;
//
//    /**
//     * Распарсивает буфер заголовка, если успешно - в переменных фрейма - значения.
//     * @return 0 - успешно, иначе - код ошибки (>0).
//     */
//    int parseHeader() {
//        int b1 = bbH.get(0x31);
//        int b2 = bbH.get(0x32);
//        int b3 = bbH.get(0x33);
//        if (b1 != 'C' || b2 != 'A' || b3 != 'M') {
//            return 1;
//        }
//        // Номер камеры (+допинфа в старшем байте? игнорируем).
//        cf_CamNumber = (bbH.getInt(0x8) & 0xFF) + 1;
//        if (cf_CamNumber < 1 || cf_CamNumber > MAXCAMS) {
//            return 2;
//        }
//        cf_FPS = bbH.get(0x12);
//        if (cf_FPS < 0 || cf_FPS > 30) {
//            return 3;
//        }
//        int mf = bbH.get(0x13);
//        if (mf > 1) { // MainFrame
//            return 4;
//        }
//        cf_VideoSize = bbH.getInt(0x19); // Размер кадра видеоданных.
//        if (cf_VideoSize < 0 || cf_VideoSize > 1000000) {
//            return 5;
//        }
//        cf_AudioSize = bbH.getInt(0x1D); // Размер кадра аудиоданных.
//        if (cf_AudioSize < 0 || cf_AudioSize > 1000000) {
//            return 6;
//        }
//        cf_Number = bbH.getInt(0x2D); // Номер кадра.
//        cf_Time = getDate(bbH.getInt(4)); // Дата-время (смещение в секундах от 1970 г.)
//        cf_isMainFrame = mf == 0 ? true : false; // Базовый кадр.
//        return 0;
//    }
//
//    boolean parseHddFrame() {
//        try {
//            readIn(baHeader, HDD_HSIZE);
//            curPos += HDD_HSIZE;
//
//            if (parseHeader() != 0) {
//                return false;
//            }
//
//            int n = cf_CamNumber - 1;
//            info[n].isExists = true;
//            info[n].framesCount++;
//            info[n].framesSize += cf_VideoSize + cf_AudioSize;
//            if (info[n].minTime == null || info[n].minTime.after(cf_Time)) {
//                info[n].minTime = cf_Time;
//            }
//            if (info[n].maxTime == null || info[n].maxTime.before(cf_Time)) {
//                info[n].maxTime = cf_Time;
//            }
//
////            log("Cam=" + cf_CamNumber + " Frame=" + cf_Number + " DT="+ cf_Time + " SizeV=" + cf_VideoSize + " SizeA=" + cf_AudioSize);
//
//            curPos += cf_VideoSize + cf_AudioSize;
//            if (cf_CamNumber == camNumber && (fout != null || procOut != null)) {
//                if (cf_VideoSize + cf_AudioSize > baFrame.length) {
//                    log("Размер фрейма превышает размер буфера! = " + (cf_VideoSize + cf_AudioSize));
//                    return false;
//                }
//                readIn(baFrame, cf_VideoSize + cf_AudioSize);
//                if (isAudio) {
//                    if (fout != null) {
//                        fout.write(baFrame, 0, cf_VideoSize + cf_AudioSize);
//                    }
//                    if (procOut != null) {
//                        procOut.write(baFrame, 0, cf_VideoSize + cf_AudioSize);
//                    }
//                } else {
//                    if (fout != null) {
//                        fout.write(baFrame, 0, cf_VideoSize);
//                    }
//                    if (procOut != null) {
//                        procOut.write(baFrame, 0, cf_VideoSize);
//                    }
//                }
//                if (fsrt != null) {
//                    if (srtTime == null) {
//                        srtTime = cf_Time;
//                    } else {
//                        if (cf_Time.getTime() - srtTime.getTime() >= 1000) {
//                            long msec1 = (long) ((double) srtFrame * 1000 / 12.5);
//                            long h1 = msec1 / 3600 / 1000;
//                            msec1 -= h1 * 3600 * 1000;
//                            long m1 = msec1 / 60 / 1000;
//                            msec1 -= m1 * 60 * 1000;
//                            long s1 = msec1 / 1000;
//                            msec1 -= s1 * 1000;
//                            long msec2 = (long) ((double) (info[n].framesCount - 1) * 1000 / 12.5) - 1;
//                            long h2 = msec2 / 3600 / 1000;
//                            msec2 -= h2 * 3600 * 1000;
//                            long m2 = msec2 / 60 / 1000;
//                            msec2 -= m2 * 60 * 1000;
//                            long s2 = msec2 / 1000;
//                            msec2 -= s2 * 1000;
//                            fsrt.printf("%9$d\n%1$02d:%2$02d:%3$02d,%4$03d --> %5$02d:%6$02d:%7$02d,%8$03d\n",
//                                    h1, m1, s1, msec1, h2, m2, s2, msec2, srtFrameCount);
//                            fsrt.printf("%1$td.%1$tm.%1$tY %1$tH:%1$tM:%1$tS\n", srtTime);
//                            long nn = info[n].framesCount - 1 - srtFrame;
//                            fsrt.printf("st=%1$tM:%1$tS %2$tM:%2$tS sf=%3$d %4$d [%5$d]\n\n", srtTime, cf_Time, srtFrame, info[n].framesCount - 1, nn);
//                            if (nn != cf_FPS) {
//                                log(String.format("st=%1$tM:%1$tS %2$tM:%2$tS sf=%3$d %4$d [%5$d] fps=%6$d",
//                                        srtTime, cf_Time, srtFrame, info[n].framesCount - 1, nn, cf_FPS));
//                            }
//                            srtTime = cf_Time;
//                            srtFrame = info[n].framesCount - 1;
//                            srtFrameCount++;
//                        }
//                    }
//                }
//
//
//            } else {
//                skipIn(cf_VideoSize + cf_AudioSize);
//            }
//
//            frameCount++;
//            frameInStepCount++;
//            if (frameInStepCount >= 5000) {
//                frameInStepCount = 0;
//                //long progress = 100L * (curPos - startDataPos) / (endDataPos - startDataPos);
//                //log("Frame=" + cf_Number + " DT=" + cf_Time + " [" + progress + "%]");
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//            return false;
//        }
//        return true;
//    }
//
//    /**
//     * Обработка конкретного файла с HDD.
//     * @return 
//     */
//    int parseHdd() {
//        try {
//            File f = new File(sInput);
//            if (!f.exists()) {
//                log("Файл-источник не существует! " + sInput);
//                return 1;
//            }
//            if (!f.canRead()) {
//                log("Файл-источник не доступен для чтения! " + sInput);
//                return 2;
//            }
//
//            ////////////////////////////////////////////////////////////////////
//            // Считываем информацию о данных.
//            ////////////////////////////////////////////////////////////////////
//            in = new RandomAccessFile(f, "r");
//            in.seek(0);
//
//            // Наличие треков камер.
//            for (int i = 0; i < MAXCAMS; i++) {
//                info[i] = new CamInfo();
//                info[i].isExists = false;
//                info[i].framesCount = 0;
//                info[i].framesSize = 0;
//            }
//            // Позиции начала и конца данных в файле.
//            startDataPos = 0;
//            endDataPos = f.length();
//            log("Общий размер данных = " + (endDataPos - startDataPos));
//
//
//            ////////////////////////////////////////////////////////////////////
//            // Считываем данные.
//            ////////////////////////////////////////////////////////////////////
//            log("CAM" + camNumber + " обработка...");
//
//            if (sVideo != null) {
//                File ff = new File(sVideo);
//                if (ff.exists()) {
//                    ff.delete();
//                }
//                proc = Runtime.getRuntime().exec(
//                        new String[]{"ffmpeg", "-vcodec", "copy", "-r", "12.5",
//                            "-i", "-", sVideo});
//                procOut = proc.getOutputStream();
//            }
//
//            if (sFile != null) {
//                File ff = new File(sFile);
//                if (ff.exists()) {
//                    ff.delete();
//                }
//                fout = new FileOutputStream(sFile);
//            }
//
//            if (sSRT != null) {
//                File ff = new File(sSRT);
//                if (ff.exists()) {
//                    ff.delete();
//                }
//                fsrt = new PrintStream(ff);
//            }
//
//            curPos = startDataPos;
//            long ecount = 0;
//            while (curPos < endDataPos - HDD_HSIZE) {
//                long saveCurPos = curPos;
//                if (!parseHddFrame()) {
//                    if (ecount == 0) {
//                        log("ERROR startpos = " + saveCurPos);
//                    }
//                    curPos = saveCurPos + 1;
//                    in.seek(curPos);
//                    ecount++;
//                } else {
//                    if (ecount > 0) {
//                        log("Ecount = " + ecount + " okpos=" + saveCurPos);
//                        ecount = 0;
//                    }
//                }
//            }
//
//            if (fout != null) {
//                fout.flush();
//                fout.close();
//            }
//
//            if (fsrt != null) {
//                fsrt.flush();
//                fsrt.close();
//            }
//
//            if (procOut != null) {
//                procOut.flush();
//                procOut.close();
//                try {
//                    proc.exitValue();
//                } catch (IllegalThreadStateException ee) {
//                    log("Ожидание завершения обработки ffmpeg...");
//                    proc.waitFor();
//                    log("... обработка ffmpeg завершена.");
//                }
//            }
//            in.close();
//
//            for (int i = 0; i < MAXCAMS; i++) {
//                if (info[i].isExists) {
//                    log("CAM" + (i + 1) + " Count=" + info[i].framesCount
//                            + " Size=" + info[i].framesSize
//                            + " Time(h)=" + info[i].framesCount / 12.5 / 3600
//                            + " [" + info[i].minTime + " - " + info[i].maxTime + "]");
//                }
//            }
//
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            return -1; // Прочие ошибки исполнения.
//        }
//
//        return 0;
//    }
//
//
    static {
        String laf = "javax.swing.plaf.metal.MetalLookAndFeel";
        //String laf = "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel";
        //String laf = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";

        // Отключение жирного шрифта в UI.
        //UIManager.put("swing.boldMetal", Boolean.FALSE);
        //UIManager.put("swing.useSystemFontSettings", Boolean.TRUE);
        //UIManager.put("swing.metalTheme", "steel");

        // Отключение жирного шрифта в UI.
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        // Фон кнопок - вместо уродского градиента.
        UIManager.put("Button.background", new Color(0xF8F8F8));
        UIManager.put("ToggleButton.background", new Color(0xF8F8F8));
        UIManager.put("ToolTip.background", GUI.colorToolTipBg);
        UIManager.put("ToolTip.foreground", GUI.colorToolTipFg);
        UIManager.put("ToolTip.border", GUI.borderToolTip);
        //JFrame.setDefaultLookAndFeelDecorated(true);
        //JDialog.setDefaultLookAndFeelDecorated(true);

        try {
            Class c = Class.forName(laf);
            UIManager.setLookAndFeel(laf);
        } catch (java.lang.ClassNotFoundException e) {
            App.log("Не найден L&F (" + laf + ")! " + e);
        } catch (Exception e) {
            App.log("Ошибка включения L&F (" + laf + ")!" + e);
        }
    }

    /**
     * Точка запуска приложения.
     * @param Аргументы.
     */
    public static void main(String[] args) {

        Calendar c = Calendar.getInstance();
        c.set(2005, 00, 01, 0, 0, 0);
        int t1 = Frame.getDate(c.getTime());
        Date d1 = Frame.getDate(t1);
        log("Time=" + c.getTime() + " t1=" + t1 + " dt=" + d1);
//        if (true) return;


        App app = new App();
        // Старт многооконного приложения
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                // Позиционируем по центру экрана
                mainFrame = new MainFrame();
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                mainFrame.setLocation(
                        new Point((screenSize.width - mainFrame.getWidth()) / 2,
                        (screenSize.height - mainFrame.getHeight()) / 2));
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
