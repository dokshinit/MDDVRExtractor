package dvrextract;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

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
            stopFFMpegProcess();
        } catch (FFMpegException ex) {
            App.log(ex.getMessage());
        }
    }

    private static void startFFMpegProcess() throws FFMpegException {
        if (process == null) {
            // Оригинальный fps.
            String fps = String.valueOf(fileInfo.frameFirst.fps);
            // Оригинальный размер кадра.
            String size = "704x576"; // TODO: Переделать в реальную подстановку!
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
                // Выход!
                throw new FFMpegException("Ошибка запуска FFMpeg!");
            }
        }
    }

    private static void stopFFMpegProcess() throws FFMpegException {
        if (process != null) {
            try {
                processOut.flush();
                processOut.close();
            } catch (IOException ex) {
            }
            // 1 сек. ожидание выхода и форсированное снятие процесса!
            // TODO: Сделать окно с вопросом о снятии!
            for (int i = 0; i < 10; i++) {
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
            process.destroy();
            throw new FFMpegException("FFMpeg завершен принудительно!");
        }
    }

    private static void processFile(FileInfo fileinfo) throws FFMpegException, SourceException {
        if (process == null) {
            startFFMpegProcess();
        }
        InputData in = null;
        try {
            in = new InputData(fileinfo.fileName);
        } catch (FileNotFoundException ex) {
            throw new SourceException("");
        } catch (IOException ex) {
            throw new SourceException("");
        }

        // * 2. Цикл обработки (последовательно обрабатываем все файлы списка):
        // * 2.1. Берем фрейм из файла.
        // * 2.2. Записываем данные фрейма в процесс ffmpeg.
        // * 2.3. Проверяем и сохраняем выходные данные из ffmpeg.
        // * 3. Закрываем входной поток ffmpeg. Сохраняем выходные данные. Закрываем процесс.
        try {
            // Буфер чтения и парсинга данных.
            final byte[] baFrame = new byte[100000];
            final ByteBuffer bbF = ByteBuffer.wrap(baFrame);
            bbF.order(ByteOrder.LITTLE_ENDIAN);
            fileInfo = fileinfo;

            int cam = App.srcCamSelect;
            Frame f = new Frame(fileInfo.fileType);
            int frameSize = f.getHeaderSize();
            long pos = fileInfo.frameFirst.pos; // Текущая позиция.
            long endpos = fileInfo.endDataPos; // Последняя позиция.
            long ost = endpos - pos; // Размер данных к обработке.

            // Идём по кадрам от начала к концу.
            while (pos < endpos) {
                in.seek(pos);
                int len = (int) Math.min(baFrame.length, ost);
                in.read(baFrame, (int) len);
                int i = 0;
                for (; i < len - frameSize; i++) {
                    if (f.parseHeader(bbF, i) == 0) {
                        // Если номер камеры указан и это не он - пропускаем.
                        if (f.camNumber == cam) {
                            if (f.isMain || (frame != null && frame.frameParsedCount > 0))
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
