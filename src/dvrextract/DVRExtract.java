/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dvrextract;

import java.awt.Dimension;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Date;
import java.util.TimeZone;
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
    //
    RandomAccessFile in = null;
    FileOutputStream fout = null;
    Process proc = null;
    OutputStream procOut = null;
    int frameCount = 0;
    int frameInStepCount = 0;
    //
    boolean[] isCams = new boolean[16];
    int startDataPos = 0;
    int endDataPos = 0;
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

    /**
     * Преобразование даты регистратора в дату Java.
     * Дата хранится в формате int - кол-во секунд от 01.01.1970.
     * @return Дата и время в формате джавы или null при ошибке.
     */
    public Date getDate(int bdate) {
        try {
            // Часовой пояс для вычисления коррекции к мировому времени.
            TimeZone curZone = TimeZone.getDefault();
            // Приведение к дате в счислении Java: кол-во мс от 01.01.1970.
            // отнимаем 60 минут чтобы компенсировать ленее время - ПРОКОНТРОЛИРОВАТЬ НА СТЫКЕ!
            long javaDate = (long) bdate * 1000 - 3600000;
            // Внесение коррекции на часовой пояс.
            // НЕ учитывается переход на летнее и зимнее время, т.к. исходная
            // информация уже с учётом летнего времени!
            return new Date(javaDate - curZone.getRawOffset());
        } catch (Exception e) {
            return null;
        }
    }
    Date cf_Time; // Дата-время (смещение в секундах от 1970 г.)
    int cf_CamNumber; // Номер камеры (+допинфа в старшем байте? игнорируем).
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
            if (cf_CamNumber < 1 || cf_CamNumber > isCams.length) {
                log("Возможное нарушение целостности данных! "
                        + "Номер камеры вне диапазона [1..32] = " + cf_CamNumber);
                return false;
            }
            if (!isCams[cf_CamNumber - 1]) {
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
            for (int i = 0; i < 16; i++) {
                isCams[i] = bbF.getInt() != 0 ? true : false;
                if (isCams[i]) {
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
            if (isCams[camNumber - 1]) {
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
    public void main(String[] args) {
        camNumber = 6;
        sInput = "/home/work/files/AZS2/87-.exe";
        
        DVRExtract dvr = new DVRExtract();
        //dvr.parse();
        
        dvr.init();
        dvr.setVisible(true);

    }
    //TODO: Два режима работы - графический и консольный.
    //TODO: Прикрутить ключи для консольного использования.
    //TODO: Сделать просмотр или просто первые кадры камер? Средства?
}
