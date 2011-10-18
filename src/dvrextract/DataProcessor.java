package dvrextract;

import java.awt.Dimension;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import javax.swing.JOptionPane;

/**
 * Процесс обработки данных.
 * 
 * @author lex
 */
public class DataProcessor {

    ////////////////////////////////////////////////////////////////////////////
    // Процесс FFMPEG обрабатывающий данные.
    private static Process process;
    private static InputStream processIn;
    private static OutputStream processOut;
    ////////////////////////////////////////////////////////////////////////////
    // Текущая инфа о камере.
    private static CamInfo camInfo;
    // Текущий обрабатываемый файл.
    private static FileInfo fileInfo;
    // Последний обработанный фрейм.
    private static Frame frame;
    ////////////////////////////////////////////////////////////////////////////
    // Кол-во распарсеных кадров (кол-во вызовов парсера).
    public static long frameParsedCount;
    // Кол-во обработанных кадров (сохранённых).
    public static long frameProcessCount;
    // Размер обработанных данных.
    public static long videoProcessSize;
    public static long audioProcessSize;
    // Минимальное и максимальное время среди обработанных кадров.
    public static long timeMin, timeMax;
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Обработка данных.
     */
    public static void process() {
        process = null;
        frameParsedCount = 0;
        frameProcessCount = 0;
        videoProcessSize = 0;
        audioProcessSize = 0;
        timeMin = -1;
        timeMax = -1;
        // Проверка и обработка в хронологическом порядке списка файлов.
        int cam = App.srcCamSelect;
        camInfo = App.srcCams[cam];

        for (int i = 0; i < camInfo.files.size(); i++) {
            if (Task.isTerminate()) {
                break;
            }
            FileInfo fi = camInfo.files.get(i);
            if (!fi.frameFirst.time.after(App.destTimeEnd)
                    && !fi.frameLast.time.before(App.destTimeStart)) {
                try {
                    processFile(fi);
                } catch (Exception ex) {
                    App.log(ex.getMessage());
                    break;
                }
            }
        }
        try {
            // Останов процесса FFMpeg.
            stopFFMpegProcess();
        } catch (FFMpegException ex) {
            App.log(ex.getMessage());
        }
    }

    /**
     * Запуск процесса FFMpeg c параметрами для обработки.
     * @throws dvrextract.DataProcessor.FFMpegException Ошибка выполнения операции.
     */
    private static void startFFMpegProcess() throws FFMpegException {
        if (process == null) {
            // Оригинальный fps.
            String fps = String.valueOf(fileInfo.frameFirst.fps);
            // Оригинальный размер кадра.
            Dimension d = fileInfo.frameFirst.getResolution();
            String size = "" + d.width + "x" + d.height;
            // Компилируем командную строку для ffmpeg.
            StringBuilder cmd = new StringBuilder("ffmpeg -i - ");
            cmd.append(App.destVideoOptions.replace("{origfps}", fps).replace("{origsize}", size));
            cmd.append(" ");
            cmd.append(App.destAudioOptions);
            cmd.append(" ");
            cmd.append(App.destName);

            // Стартуем процесс обработки.
            try {
                process = Runtime.getRuntime().exec(cmd.toString());
                processIn = process.getInputStream();
                processOut = process.getOutputStream();

            } catch (IOException ex) {
                if (process != null) {
                    process.destroy();
                    process = null;
                }
                throw new FFMpegException("Ошибка запуска FFMpeg!");
            }
        }
    }

    /**
     * Завершение процесса FFMpeg. Сначала закрывает принимающий поток, потом
     * ожидает завершения процесса и при истечении таймаута, если процесс не
     * завершился - завершает его принудительно.
     * @throws dvrextract.DataProcessor.FFMpegException 
     */
    private static void stopFFMpegProcess() throws FFMpegException {
        if (process != null) {
            try {
                processOut.flush();
                processOut.close();
            } catch (IOException ex) {
            }
            while (true) {
                // 10 сек. ожидание выхода и запрос на форсированное снятие процесса!
                for (int i = 0; i < 100; i++) {
                    try {
                        process.exitValue();
                        return;
                    } catch (IllegalThreadStateException e) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException ex) {
                        }
                    }
                }
                Object[] options = {"Завершить", "Ожидать завершения"};
                int n = JOptionPane.showOptionDialog(App.mainFrame,
                        "Завершить процесс FFMpeg принудительно или ожидать ещё 10 сек.?",
                        "Подтверждение", JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE, null, options,
                        options[1]);
                if (n == 0) {
                    process.destroy();
                    throw new FFMpegException("FFMpeg завершен принудительно!");
                }
            }
        }
    }

    private static void processFile(FileInfo fileinfo) throws FFMpegException, SourceException {
        if (process == null) {
            startFFMpegProcess();
        }
        InputBufferedFile in = null;
        try {
            in = new InputBufferedFile(fileinfo.fileName, 1000000, 100);
        } catch (FileNotFoundException ex) {
            throw new SourceException("");
        } catch (IOException ex) {
            throw new SourceException("");
        }

        try {
            fileInfo = fileinfo;

            int cam = App.srcCamSelect;
            Frame f = new Frame(fileInfo.fileType);
            int frameSize = f.getHeaderSize();
            long pos = fileInfo.frameFirst.pos; // Текущая позиция.
            long endpos = fileInfo.endDataPos; // Последняя позиция.

            // Буфер чтения и парсинга данных.
            final byte[] baFrame = new byte[1000000];
            final ByteBuffer bbF = ByteBuffer.wrap(baFrame);
            bbF.order(ByteOrder.LITTLE_ENDIAN);

            // Идём по кадрам от начала к концу.
            for (; pos < endpos - frameSize; pos++) {
                in.seek(pos);
                in.read(baFrame, frameSize);
                if (f.parseHeader(bbF, 0) == 0) {
                    // Берем только фреймы выбранной камеры (актуально для EXE файла).
                    if (f.camNumber == cam) {
                        // Если это не ключевой кадр и в выводе пусто - пропускаем.
                        // TODO Доработать логику! Т.к. может случится так, что первый кадр файла не ключевой,
                        // а продолжение предыдущего, но предыдущего нет, а есть предпредыдущий - будет 
                        // неверным добавлять этот кадр.
                        long time = f.time.getTime();
                        // Отбрасываем кадры, которые ранее последнего записанного кадра 
                        // (направление времени только на увеличение!)
                        if (timeMax == -1 || time > timeMax) {
                            if (f.isMain || (frame != null && frameProcessCount > 0)) {
                                in.read(baFrame, f.videoSize + f.audioSize);
                                int audio = (App.destAudioType != -1 ? f.audioSize : 0);
                                processOut.write(baFrame, 0, f.videoSize + audio);
                                if (App.destSubType != -1) {
                                    writeSub();
                                }
                                frame = f;
                                frameProcessCount++;
                                videoProcessSize += f.videoSize;
                                audioProcessSize += audio;
                                timeMin = (timeMin == -1) ? time : timeMin;
                                timeMax = time;
                            }
                        }
                    }
                    pos += frameSize + f.videoSize + f.audioSize - 1; // -1 т.к. автоинкремент.
                }
                frameParsedCount++;
            }
            //App.log("CAM"+info.camNumber+" file="+info.fileName);
            in.close();

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * Если для текущего фрейма необходимо - формирует субтитры согласно 
     * настройкам и сохраняет их в файл.
     */
    private static void writeSub() {
        //
    }

    public final static class FFMpegException extends Exception {

        public FFMpegException(String msg) {
            super(msg);
        }
    }

    public final static class SourceException extends Exception {

        public SourceException(String msg) {
            super(msg);
        }
    }
}
