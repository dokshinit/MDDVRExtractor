/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dvrextract;

import java.awt.Dimension;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Date;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.regex.Pattern;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author lex
 */
public class DVRExtract extends JFrame {

    final byte[] baHeader = new byte[93];
    final ByteBuffer bbH = ByteBuffer.wrap(baHeader);
    final byte[] baFrame = new byte[1000000]; // один фрейм
    final ByteBuffer bbF = ByteBuffer.wrap(baFrame);

    {
        bbH.order(ByteOrder.LITTLE_ENDIAN);
        bbF.order(ByteOrder.LITTLE_ENDIAN);
    }
    int camNumber = 0;
    boolean isAudio = false;
    String sInput = null;
    String sFile = null;
    String sVideo = null;
    String sSRT = null;
    //
    RandomAccessFile in = null;
    FileOutputStream fout = null;
    PrintStream fsrt = null;
    Process proc = null;
    OutputStream procOut = null;
    int frameCount = 0;
    int frameInStepCount = 0;
    //

    class CamInfo {

        boolean isExists;
        long framesCount;
        long framesSize;
        Date minTime, maxTime;

        public CamInfo() {
            isExists = false;
            framesCount = 0;
            framesSize = 0;
            minTime = null;
            maxTime = null;
        }
    }
    public final static int MAXCAMS = 16;
    CamInfo[] info = new CamInfo[MAXCAMS];
    //boolean[] isCams = new boolean[16];
    long startDataPos = 0;
    long endDataPos = 0;
    long curPos = 0;

    /**
     * Вывод строки лога.
     * @param text Текст сообщения.
     */
    void log(String text) {
        System.out.println(text);
    }

    void incPos(int offs) {
        bbH.position(bbH.position() + offs);
    }

    void skipIn(int n) throws IOException {
        while (n > 0) {
            n -= in.skipBytes(n);
        }
    }

    void readIn(byte[] ba, int size) throws IOException {
        int readed = 1, pos = 0;
        while (readed >= 0 && pos < size) {
            readed = in.read(ba, pos, size - pos);
            pos += readed;
        }
    }

    Date cf_Time; // Дата-время (смещение в секундах от 1970 г.)
    int cf_CamNumber; // Номер камеры (+допинфа в старшем байте? игнорируем).
    int cf_FPS; // Чстота кадров в секунду.
    boolean cf_isMainFrame; // Базовый кадр.
    int cf_VideoSize; // Размер кадра видеоданных.
    int cf_AudioSize; // Размер кадра аудиоданных.
    int cf_Number; // Номер кадра.

    boolean parseFrame() {
        try {
            readIn(baHeader, baHeader.length);
            curPos += baHeader.length;

            bbH.position(4);
            // Дата-время (смещение в секундах от 1970 г.)
            cf_Time = getDate(bbH.getInt());
            // Номер камеры (+допинфа в старшем байте? игнорируем).
            cf_CamNumber = (bbH.getInt() & 0xFF) + 1;
            if (cf_CamNumber < 1 || cf_CamNumber > MAXCAMS) {
                log("Возможное нарушение целостности данных! "
                        + "Номер камеры вне диапазона [1..32] = " + cf_CamNumber);
                return false;
            }
            if (!info[cf_CamNumber - 1].isExists) {
                log("Возможное нарушение целостности данных! "
                        + "Номер камеры вне списка записанных камер = " + cf_CamNumber);
                return false;
            }
            incPos(7);
            cf_isMainFrame = bbH.get() == 0 ? true : false; // Базовый кадр.
            incPos(5);
            cf_VideoSize = bbH.getInt(); // Размер кадра видеоданных.
            cf_AudioSize = bbH.getInt(); // Размер кадра аудиоданных.
            incPos(12);
            cf_Number = bbH.getInt(); // Номер кадра.
            //incPos(16);
            //incPos(16); // Название камеры 16 байт - пока не требуется.
            //incPos(24);
            //log("Cam=" + cam + " Frame=" + number + " SizeV=" + sizev + " SizeA=" + sizea);

            curPos += cf_VideoSize + cf_AudioSize;
            if (cf_CamNumber == camNumber && (fout != null || procOut != null)) {
                if (cf_VideoSize + cf_AudioSize > baFrame.length) {
                    log("Размер фрейма превышает размер буфера! = " + (cf_VideoSize + cf_AudioSize));
                    return false;
                }
                readIn(baFrame, cf_VideoSize + cf_AudioSize);
                if (isAudio) {
                    if (fout != null) {
                        fout.write(baFrame, 0, cf_VideoSize + cf_AudioSize);
                    }
                    if (procOut != null) {
                        procOut.write(baFrame, 0, cf_VideoSize + cf_AudioSize);
                    }
                } else {
                    if (fout != null) {
                        fout.write(baFrame, 0, cf_VideoSize);
                    }
                    if (procOut != null) {
                        procOut.write(baFrame, 0, cf_VideoSize);
                    }
                }

            } else {
                skipIn(cf_VideoSize + cf_AudioSize);
            }

            frameCount++;
            frameInStepCount++;
            if (frameInStepCount >= 5000) {
                frameInStepCount = 0;
                long progress = 100L * (curPos - startDataPos) / (endDataPos - startDataPos);
                log("Frame=" + cf_Number + " [" + progress + "%]");
            }

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    int parse() {
        try {
            File f = new File(sInput);
            if (!f.exists()) {
                log("Файл-источник не существует! " + sInput);
                return 1;
            }
            if (!f.canRead()) {
                log("Файл-источник не доступен для чтения! " + sInput);
                return 2;
            }

            ////////////////////////////////////////////////////////////////////
            // Считываем информацию о данных.
            ////////////////////////////////////////////////////////////////////
            in = new RandomAccessFile(f, "r");
            in.seek(in.length() - 28 * 16);
            in.readFully(baFrame, 0, 28 * 16);

            // Наличие треков камер.
            for (int i = 0; i < MAXCAMS; i++) {
                info[i] = new CamInfo();
                info[i].isExists = bbF.getInt() != 0 ? true : false;
                info[i].framesCount = 0;
                info[i].framesSize = 0;
                if (info[i].isExists) {
                    log("CAM" + (i + 1) + " данные в наличии!");
                }
            }
            // Позиции начала и конца данных в файле.
            startDataPos = bbF.getInt();
            endDataPos = bbF.getInt(12 * 16);
            log("Общий размер данных = " + (endDataPos - startDataPos));


            ////////////////////////////////////////////////////////////////////
            // Считываем данные.
            ////////////////////////////////////////////////////////////////////
            if (info[camNumber - 1].isExists) {
                log("CAM" + camNumber + " обработка...");

                if (sVideo != null) {
                    proc = Runtime.getRuntime().exec(
                            new String[]{"ffmpeg", "-vcodec", "copy", "-r", "12.5",
                                "-i", "-", sVideo});
                    procOut = proc.getOutputStream();
                }

                if (sFile != null) {
                    fout = new FileOutputStream(sFile);
                }

                in.seek(startDataPos);
                curPos = startDataPos;

                while (curPos < endDataPos) {
                    if (!parseFrame()) {
                        break;
                    }
                }

                if (fout != null) {
                    fout.flush();
                    fout.close();
                }

                if (procOut != null) {
                    procOut.flush();
                    procOut.close();
                    try {
                        proc.exitValue();
                    } catch (IllegalThreadStateException ee) {
                        log("Ожидание завершения обработки ffmpeg...");
                        proc.waitFor();
                        log("... обработка ffmpeg завершена.");
                    }
                }

            } else {
                log("Файл-источник не содержит данных для CAM" + camNumber + ".");
                return 3;
            }
            in.close();

        } catch (Exception ex) {
            ex.printStackTrace();
            return -1; // Прочие ошибки исполнения.
        }

        return 0;
    }
    //
    final int HDD_HSIZE = 77;
    Date srtTime = null;
    long srtFrame = 0;
    long srtFrameCount = 0;

    /**
     * Распарсивает буфер заголовка, если успешно - в переменных фрейма - значения.
     * @return 0 - успешно, иначе - код ошибки (>0).
     */
    int parseHeader() {
        int b1 = bbH.get(0x31);
        int b2 = bbH.get(0x32);
        int b3 = bbH.get(0x33);
        if (b1 != 'C' || b2 != 'A' || b3 != 'M') {
            return 1;
        }
        // Номер камеры (+допинфа в старшем байте? игнорируем).
        cf_CamNumber = (bbH.getInt(0x8) & 0xFF) + 1;
        if (cf_CamNumber < 1 || cf_CamNumber > MAXCAMS) {
            return 2;
        }
        cf_FPS = bbH.get(0x12);
        if (cf_FPS < 0 || cf_FPS > 30) {
            return 3;
        }
        int mf = bbH.get(0x13);
        if (mf > 1) { // MainFrame
            return 4;
        }
        cf_VideoSize = bbH.getInt(0x19); // Размер кадра видеоданных.
        if (cf_VideoSize < 0 || cf_VideoSize > 1000000) {
            return 5;
        }
        cf_AudioSize = bbH.getInt(0x1D); // Размер кадра аудиоданных.
        if (cf_AudioSize < 0 || cf_AudioSize > 1000000) {
            return 6;
        }
        cf_Number = bbH.getInt(0x2D); // Номер кадра.
        cf_Time = getDate(bbH.getInt(4)); // Дата-время (смещение в секундах от 1970 г.)
        cf_isMainFrame = mf == 0 ? true : false; // Базовый кадр.
        return 0;
    }

    boolean parseHddFrame() {
        try {
            readIn(baHeader, HDD_HSIZE);
            curPos += HDD_HSIZE;

            if (parseHeader() != 0) {
                return false;
            }

            int n = cf_CamNumber - 1;
            info[n].isExists = true;
            info[n].framesCount++;
            info[n].framesSize += cf_VideoSize + cf_AudioSize;
            if (info[n].minTime == null || info[n].minTime.after(cf_Time)) {
                info[n].minTime = cf_Time;
            }
            if (info[n].maxTime == null || info[n].maxTime.before(cf_Time)) {
                info[n].maxTime = cf_Time;
            }

//            log("Cam=" + cf_CamNumber + " Frame=" + cf_Number + " DT="+ cf_Time + " SizeV=" + cf_VideoSize + " SizeA=" + cf_AudioSize);

            curPos += cf_VideoSize + cf_AudioSize;
            if (cf_CamNumber == camNumber && (fout != null || procOut != null)) {
                if (cf_VideoSize + cf_AudioSize > baFrame.length) {
                    log("Размер фрейма превышает размер буфера! = " + (cf_VideoSize + cf_AudioSize));
                    return false;
                }
                readIn(baFrame, cf_VideoSize + cf_AudioSize);
                if (isAudio) {
                    if (fout != null) {
                        fout.write(baFrame, 0, cf_VideoSize + cf_AudioSize);
                    }
                    if (procOut != null) {
                        procOut.write(baFrame, 0, cf_VideoSize + cf_AudioSize);
                    }
                } else {
                    if (fout != null) {
                        fout.write(baFrame, 0, cf_VideoSize);
                    }
                    if (procOut != null) {
                        procOut.write(baFrame, 0, cf_VideoSize);
                    }
                }
                if (fsrt != null) {
                    if (srtTime == null) {
                        srtTime = cf_Time;
                    } else {
                        if (cf_Time.getTime() - srtTime.getTime() >= 1000) {
                            long msec1 = (long) ((double) srtFrame * 1000 / 12.5);
                            long h1 = msec1 / 3600 / 1000;
                            msec1 -= h1 * 3600 * 1000;
                            long m1 = msec1 / 60 / 1000;
                            msec1 -= m1 * 60 * 1000;
                            long s1 = msec1 / 1000;
                            msec1 -= s1 * 1000;
                            long msec2 = (long) ((double) (info[n].framesCount - 1) * 1000 / 12.5) - 1;
                            long h2 = msec2 / 3600 / 1000;
                            msec2 -= h2 * 3600 * 1000;
                            long m2 = msec2 / 60 / 1000;
                            msec2 -= m2 * 60 * 1000;
                            long s2 = msec2 / 1000;
                            msec2 -= s2 * 1000;
                            fsrt.printf("%9$d\n%1$02d:%2$02d:%3$02d,%4$03d --> %5$02d:%6$02d:%7$02d,%8$03d\n",
                                    h1, m1, s1, msec1, h2, m2, s2, msec2, srtFrameCount);
                            fsrt.printf("%1$td.%1$tm.%1$tY %1$tH:%1$tM:%1$tS\n", srtTime);
                            long nn = info[n].framesCount - 1 - srtFrame;
                            fsrt.printf("st=%1$tM:%1$tS %2$tM:%2$tS sf=%3$d %4$d [%5$d]\n\n", srtTime, cf_Time, srtFrame, info[n].framesCount - 1, nn);
                            if (nn != cf_FPS) {
                                log(String.format("st=%1$tM:%1$tS %2$tM:%2$tS sf=%3$d %4$d [%5$d] fps=%6$d",
                                        srtTime, cf_Time, srtFrame, info[n].framesCount - 1, nn, cf_FPS));
                            }
                            srtTime = cf_Time;
                            srtFrame = info[n].framesCount - 1;
                            srtFrameCount++;
                        }
                    }
                }


            } else {
                skipIn(cf_VideoSize + cf_AudioSize);
            }

            frameCount++;
            frameInStepCount++;
            if (frameInStepCount >= 5000) {
                frameInStepCount = 0;
                //long progress = 100L * (curPos - startDataPos) / (endDataPos - startDataPos);
                //log("Frame=" + cf_Number + " DT=" + cf_Time + " [" + progress + "%]");
            }

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Обработка конкретного файла с HDD.
     * @return 
     */
    int parseHdd() {
        try {
            File f = new File(sInput);
            if (!f.exists()) {
                log("Файл-источник не существует! " + sInput);
                return 1;
            }
            if (!f.canRead()) {
                log("Файл-источник не доступен для чтения! " + sInput);
                return 2;
            }

            ////////////////////////////////////////////////////////////////////
            // Считываем информацию о данных.
            ////////////////////////////////////////////////////////////////////
            in = new RandomAccessFile(f, "r");
            in.seek(0);

            // Наличие треков камер.
            for (int i = 0; i < MAXCAMS; i++) {
                info[i] = new CamInfo();
                info[i].isExists = false;
                info[i].framesCount = 0;
                info[i].framesSize = 0;
            }
            // Позиции начала и конца данных в файле.
            startDataPos = 0;
            endDataPos = f.length();
            log("Общий размер данных = " + (endDataPos - startDataPos));


            ////////////////////////////////////////////////////////////////////
            // Считываем данные.
            ////////////////////////////////////////////////////////////////////
            log("CAM" + camNumber + " обработка...");

            if (sVideo != null) {
                File ff = new File(sVideo);
                if (ff.exists()) {
                    ff.delete();
                }
                proc = Runtime.getRuntime().exec(
                        new String[]{"ffmpeg", "-vcodec", "copy", "-r", "12.5",
                            "-i", "-", sVideo});
                procOut = proc.getOutputStream();
            }

            if (sFile != null) {
                File ff = new File(sFile);
                if (ff.exists()) {
                    ff.delete();
                }
                fout = new FileOutputStream(sFile);
            }

            if (sSRT != null) {
                File ff = new File(sSRT);
                if (ff.exists()) {
                    ff.delete();
                }
                fsrt = new PrintStream(ff);
            }

            curPos = startDataPos;
            long ecount = 0;
            while (curPos < endDataPos - HDD_HSIZE) {
                long saveCurPos = curPos;
                if (!parseHddFrame()) {
                    if (ecount == 0) {
                        log("ERROR startpos = " + saveCurPos);
                    }
                    curPos = saveCurPos + 1;
                    in.seek(curPos);
                    ecount++;
                } else {
                    if (ecount > 0) {
                        log("Ecount = " + ecount + " okpos=" + saveCurPos);
                        ecount = 0;
                    }
                }
            }

            if (fout != null) {
                fout.flush();
                fout.close();
            }

            if (fsrt != null) {
                fsrt.flush();
                fsrt.close();
            }

            if (procOut != null) {
                procOut.flush();
                procOut.close();
                try {
                    proc.exitValue();
                } catch (IllegalThreadStateException ee) {
                    log("Ожидание завершения обработки ffmpeg...");
                    proc.waitFor();
                    log("... обработка ffmpeg завершена.");
                }
            }
            in.close();

            for (int i = 0; i < MAXCAMS; i++) {
                if (info[i].isExists) {
                    log("CAM" + (i + 1) + " Count=" + info[i].framesCount
                            + " Size=" + info[i].framesSize
                            + " Time(h)=" + info[i].framesCount / 12.5 / 3600
                            + " [" + info[i].minTime + " - " + info[i].maxTime + "]");
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            return -1; // Прочие ошибки исполнения.
        }

        return 0;
    }
    //SortedMap<long, FileInfo> files = new SortedMap<long, FileInfo>() {};
    // Фильтр для отбора обрабатываемых файлов: daNNNNN - где N - номер файла.
    final Pattern ptrn = Pattern.compile("da+",
            Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);
    ArrayList<HDDFileInfo>[] camFiles = new ArrayList[MAXCAMS];
    ArrayList<HDDFileInfo> files = new ArrayList<HDDFileInfo>();

    /**
     * Распознавание начального и конечного кадров файла.
     * Создание записи информации о файле и добавление её в массив соответсвенной камере.
     * @param fileName Имя файла.
     */
    void parseHDDFileInfo(String fileName) {
        curPos = 0;
        // Ищем первый кадр.
        // Ищем последний кадр.
        
        
    }
    
    /**
     * Рекурсивное построение списка файлов по фильтру (с обходом подкаталогов).
     * @param path Путь к каталогу сканирования.
     */
    void scanHDDFiles(String path) {
        try {
            File f = new File(path);
            File[] fa = f.listFiles(new FileFilter() {

                @Override
                public boolean accept(File pathname) {
                    if (pathname.isDirectory()) {
                        return true;
                    }
                    return ptrn.matcher(pathname.getName()).matches();
                }
            });

            for (int i = 0; i < fa.length; i++) {
                if (fa[i].isDirectory()) {
                    scanHDDFiles(fa[i].getPath());
                } else {
                    // Обработка файлов.
                    parseHDDFileInfo(fa[i].getPath());
                }
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    //
    //
    JButton buttonInput, buttonFile, buttonVideo;
    JTextField textInput, textFile, textVideo;
    JComboBox comboCams;
    JCheckBox checkAudio;
    JButton buttonProcess, buttonStop;
    JProgressBar progressBar;

    void init() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new MigLayout());
        setPreferredSize(new Dimension(300, 300));

        textInput = new JTextField(50);
        buttonInput = new JButton("Выбор");

        textFile = new JTextField(50);
        buttonFile = new JButton("Выбор");

        textVideo = new JTextField(50);
        buttonVideo = new JButton("Выбор");

        comboCams = new JComboBox();
        checkAudio = new JCheckBox("Включать аудиоданные.");

        buttonProcess = new JButton("Обработка");

        buttonStop = new JButton("Стоп");

        progressBar = new JProgressBar();

        pack();
    }

    /**
     * Точка запуска приложения.
     * @param Аргументы.
     */
    public static void main(String[] args) {

        DVRExtract dvr = new DVRExtract();
        dvr.camNumber = 6;
        dvr.sInput = "/home/work/files/AZSVIDEO/RESEARCH/rest/131/da00013";
        dvr.sVideo = "/home/work/files/AZSVIDEO/RESEARCH/rest/da00013.mkv";
        dvr.sSRT = "/home/work/files/AZSVIDEO/RESEARCH/rest/da00013.srt";
        dvr.parseHdd();

        //TODO: Сделать процедуру считывающую первый заголовок и последний и выдающую инфу наверх.
        //TODO: Сделать процедуру обрабатывающую все файлы в каталоге и собирающую инфу в разрезе камер.
        // к каждой камере - список файлов с определенными параметрами
        //Можно ли делать скриншоты из одного опорного кадра?
        //Каким образом в видео добавить дату-время (титрами?)?

        //dvr.init();
        //dvr.setVisible(true);

    }
    //TODO: Два режима работы - графический и консольный.
    //TODO: Прикрутить ключи для консольного использования.
    //TODO: Сделать просмотр или просто первые кадры камер? Средства?
}
