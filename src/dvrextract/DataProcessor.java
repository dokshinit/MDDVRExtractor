package dvrextract;

import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
    //private static InputStream processIn;
    private static OutputStream processOut;
    private static OutputFile rawVideo,rawAudio,rawAll;
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
        int cam = App.srcCamSelect - 1;
        camInfo = App.srcCams[cam];

        String msg = "Обработка источника...";
        App.log(msg);
        App.mainFrame.setProgressInfo(msg);
        App.mainFrame.startProgress(1, camInfo.files.size());

        try {
            for (int i = 0; i < camInfo.files.size(); i++) {
                if (Task.isTerminate()) {
                    break;
                }
                FileInfo fi = camInfo.files.get(i);

                msg = String.format("Обработка файла (%d из %d)", i + 1, camInfo.files.size());
                App.log(msg + ": " + fi.fileName);
                App.mainFrame.setProgressInfo(msg);
                App.mainFrame.setProgressText(fi.fileName);

                // Выбираем только те файлы, промежутки которых попадают в 
                if (!fi.frameFirst.time.after(App.destTimeEnd)
                        && !fi.frameLast.time.before(App.destTimeStart)
                        && (timeMax == -1 || fi.frameLast.time.getTime() >= timeMax)) {
                    try {
                        processFile(fi);
                    } catch (SourceException ex) {
                        App.log(ex.getMessage());
                        break;
                    }
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
            }

            // Останов процесса FFMpeg.
            App.log("Завершение процесса кодирования...");
            stopFFMpegProcess();
            App.logupd("Процесс кодирования завершен.");

        } catch (FFMpegException ex) {
            App.log(ex.getMessage());
        }
        App.mainFrame.stopProgress();

        msg = "Обработка источника завершена.";
        App.log(msg);
        App.mainFrame.setProgressInfo(msg);
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
            StringBuilder cmd = new StringBuilder("ffmpeg ");
            cmd.append("-r ").append(fps).append(" ");
            //cmd.append("-f pcm")
            cmd.append(" -i - ");
            cmd.append(App.destVideoOptions.replace("{origfps}", fps).replace("{origsize}", size));
            cmd.append(" ");
            cmd.append(App.destAudioOptions);
            cmd.append(" ");
            cmd.append(App.destName);
            if (!App.isDebug) {
                App.log("FFMpeg = " + cmd.toString());
            }
            try {
                rawVideo = new OutputFile("/home/work/files/probe1.video");
                rawAudio = new OutputFile("/home/work/files/probe1.audio");
                rawAll = new OutputFile("/home/work/files/probe1.out");
                File f = new File(App.destName);
                if (f.exists()) f.delete();
            } catch (FileNotFoundException ex) {
            }
            // Стартуем процесс обработки.
            try {
                process = Runtime.getRuntime().exec(cmd.toString());
                //processIn = process.getInputStream();
                processOut = process.getOutputStream();

            } catch (IOException ex) {
                if (process != null) {
                    process.destroy();
                    process = null;
                }
                Err.log(ex);
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
    @SuppressWarnings("SleepWhileInLoop")
    private static void stopFFMpegProcess() throws FFMpegException {
        if (process != null) {
            try {
                rawVideo.close();
                rawAudio.close();
                rawAll.close();
                processOut.flush();
                processOut.close();
            } catch (IOException ex) {
                Err.log(ex);
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
        //InputBufferedFile in = null;
        InputFile in = null;
        try {
            //in = new InputBufferedFile(fileinfo.fileName, 1000000, 100);
            in = new InputFile(fileinfo.fileName);
        } catch (FileNotFoundException ex) {
            throw new SourceException("File not found = " + fileinfo.fileName);
        } catch (IOException ex) {
            throw new SourceException("File IO = " + fileinfo.fileName);
        }

        fileInfo = fileinfo; // Нужно для успешного старта FFMpeg (дефолтные фпс и размеры)

        if (process == null) {
            App.log("Запуск процесса кодирования...");
            startFFMpegProcess();
            App.logupd("Запущен процесс кодирования.");
        }

        try {
            int cam = App.srcCamSelect;
            Frame f = new Frame(fileInfo.fileType);
            int frameSize = f.getHeaderSize();
            long pos = fileInfo.frameFirst.pos; // Текущая позиция.
            long endpos = fileInfo.endDataPos; // Последняя позиция.
            // Если это EXE - берём начальную позицию из инфы.
            if (fileInfo.fileType == FileType.EXE) {
                FileInfo.CamData cd = fileInfo.getCamData(cam);
                pos = cd.mainFrameOffset;
            }
            App.log("File:"+fileInfo.fileName+" start="+pos+ " end=" + endpos + " size=" + fileInfo.fileSize);

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
                        App.log("Frame pos=" + pos + " cam=" + f.camNumber 
                                + " VSz=" + f.videoSize + " ASz=" + f.audioSize
                                + " step="+(time-timeMax));
                        // Отбрасываем кадры, которые ранее последнего записанного кадра 
                        // (направление времени только на увеличение, а т.к. 
                        // дискретность времени в DVR-секунды, то неравенство не строгое!)
                        if (timeMax == -1 || time >= timeMax) {
                            // Если не было обработанных кадров - начинаем только с ключевого,
                            // если были - включаем любые.
                            if (f.isMain || frameProcessCount > 0) {
                                in.read(baFrame, f.videoSize + f.audioSize);
                                int audio = (App.destAudioType != -1 ? f.audioSize : 0);
                                processOut.write(baFrame, 0, f.videoSize + audio);
                                rawVideo.write(baFrame, 0, f.videoSize);
                                rawAudio.write(baFrame, f.videoSize, f.audioSize);
                                rawAll.write(baFrame, 0, f.videoSize + f.audioSize);
                                if (App.destSubType != -1) {
                                    //writeSub();
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
                } else {
                    App.log("Frame pos=" + pos + " Not parsed!");
                }
                frameParsedCount++;
            }
            in.close();
            App.log("Frame parsed = " + frameParsedCount);
            App.log("Frame processed = " + frameProcessCount);
            App.log("Video size = " + videoProcessSize);

        } catch (IOException ioe) {
            Err.log("File name = " + fileinfo.fileName);
            Err.log(ioe);
            throw new SourceException("File process IO = " + fileinfo.fileName);
        } finally {
            if (in != null) {
                in.closeSafe();
            }
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
