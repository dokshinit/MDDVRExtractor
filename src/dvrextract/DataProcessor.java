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
    private static CamInfo camInfo;
    // Текущий обрабатываемый файл.
    private static FileInfo fileInfo;
    // Последний обработанный фрейм.
    private static Frame frame;
    ////////////////////////////////////////////////////////////////////////////
    // Кол-во распарсеных кадров.
    public static long frameParsedCount;
    // Кол-во обработанных кадров.
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
            cmd.append(App.videoOptions.replace("{origfps}", fps).replace("{origsize}", size));
            cmd.append(" ");
            cmd.append(App.audioOptions);
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
                // 10 сек. ожидание выхода и форсированное снятие процесса!
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
            final byte[] baFrame = new byte[frameSize];
            final ByteBuffer bbF = ByteBuffer.wrap(baFrame);
            bbF.order(ByteOrder.LITTLE_ENDIAN);
            
            // Идём по кадрам от начала к концу.
            for (; pos < endpos-frameSize; pos++) {
                in.seek(pos);
                in.read(baFrame, frameSize);
                if (f.parseHeader(bbF, 0) == 0) {
                    // Берем только фреймы выбранной камеры (актуально для EXE файла).
                    if (f.camNumber == cam) {
                        // Если это не ключевой кадр и в выводе пусто - пропускаем.
                        // TODO Доработать логику! Т.к. может случится так, что первый кадр файла не ключевой,
                        // а продолжение предыдущего, но предыдущего нет, а есть предпредыдущий - будет 
                        // неверным добавлять этот кадр.
                        if (f.isMain || (frame != null && frameParsedCount > 0)) {
                                if (i + f.videoSize + f.audioSize > len) {
                                    int size1 = len - i;
                                    int size2 = f.videoSize + f.audioSize - size1;
                                    processOut.write(baFrame, i, size1);
                                    pos += len;
                                    i = 0;
                                    len = (int) Math.min(baFrame.length, ost - len);
                                    in.read(baFrame, len);
                                }
                                processOut.write(baFrame, i + frameSize, f.videoSize + f.audioSize);
                            }
                        }
                        i += f.getHeaderSize() + f.videoSize + f.audioSize - 1; // -1 т.к. автоинкремент.
                    }
                }
                pos += i;
                ost -= i;
            }
            //App.log("CAM"+info.camNumber+" file="+info.fileName);
            in.close();

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
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

    /**
     * Процесс обработки данных.
     */
    private class TaskProcess extends Task.Thread {

        @Override
        public void fireStart() {
            super.fireStart();
        }

        @Override
        public void fireStop() {
            super.fireStop();
        }

        @Override
        protected void task() {
        }
    }
}
