package dvrextract;

import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Date;
import javax.swing.JOptionPane;

/**
 * Процесс обработки данных.
 * 
 * @author lex
 */
public class DataProcessor {

    ////////////////////////////////////////////////////////////////////////////
    // Процесс FFMPEG обрабатывающий данные.
    //
    // Основной процесс - обработка видео или всего через именованные потоки.
    private static Process processVideo;
    private static OutputStream processVideoOut;
    // Процесс для обработки аудио в отдельный файл.
    private static Process processAudio;
    private static OutputStream processAudioOut;
    // Процесс для обработки субтитров в отдельный файл.
    //private static Process processSub;
    private static PrintStream processSubOut;
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
        processVideo = null;
        subTimeLast = null;
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
            App.logupd("Процесс кодирования завершён.");

        } catch (FFMpegException ex) {
            //App.log(ex.getMessage());
        }
        App.mainFrame.stopProgress();

        msg = "Обработка источника завершена.";
        App.log(msg);
        App.mainFrame.setProgressInfo(msg);
    }
    // Имена файлов для сохранения аудио и субтитров.
    private static String videoName, audioName, subName;
    // Флаги сохранения аудио и субтитров.
    private static boolean isAudio, isSub;
    // Флаги файловости вывода (иначе канал).
    private static boolean isAudioFile, isSubFile;
    // Флаги временности файлов.
    private static boolean isAudioTemp, isSubTemp;

    private static void setVars() {
        // Уникальный идентификатор файла для каналов.
        String fid = String.format("dvr%X", (int) (new Date()).getTime());
        fps = fileInfo.frameFirst.fps;

        // Аудио.
        isAudio = App.destAudioType == -1 ? false : true;
        if (App.destAudioType == 0) { // Отдельный файл.
            isAudioFile = true;
            isAudioTemp = false;
            audioName = App.destAudioName;
        } else if (App.destAudioType == 1) { // Поток.
            if (!App.isPipe) { // Отдельный промежуточный файл.
                isAudioFile = true;
                isAudioTemp = true;
                audioName = App.destVideoName + ".audio.wav";
            } else { // Именованный поток.
                isAudioFile = false;
                isAudioTemp = true;
                audioName = fid + ".audio.wav";
            }
        }
        // Субтитры.
        isSub = App.destSubType == -1 ? false : true;
        if (App.destSubType == 0) {
            isSubFile = true;
            isSubTemp = false;
            subName = App.destSubName;
        } else if (App.destSubType == 1) {
            if (!App.isPipe) {
                // Отдельный промежуточный файл.
                isSubFile = true;
                isSubTemp = true;
                subName = App.destSubName + ".sub.srt";
            } else {
                // Именованный поток.
                isSubFile = false;
                isSubTemp = true;
                subName = fid + ".sub.srt";
            }
        }
        // Если есть временные файлы - значит будет послед.слив в одно видео, 
        // значит сначала кодируем видео во временный файл.
        videoName = App.destVideoName + ((isAudio && isAudioTemp) || (isSub && isSubTemp) ? ".video.mkv" : "");
    }

    /**
     * Создание канала.
     * @param name Имя канала.
     * @return true - удачно, false - ошбика.
     */
    private static boolean createPipe(String name) {
        Process p = null;
        try {
            p = Runtime.getRuntime().exec("mkfifo " + name);
            p.waitFor();
            try {
                p.exitValue();
            } catch (IllegalArgumentException ee) {
                p.destroy();
            }
            return true;
        } catch (Exception ex) {
            try {
                p.exitValue();
            } catch (IllegalArgumentException ee) {
                p.destroy();
            }
            return false;
        }
    }

    /**
     * Удаление канала/файла.
     * @param name Имя файла.
     * @return true - успешно, false - ошибка.
     */
    private static boolean deleteFile(String name) {
        File f = new File(name);
        if (f.exists()) {
            return f.delete();
        }
        return true;
    }
    private static int fps;

    /**
     * Запуск процесса FFMpeg c параметрами для обработки.
     * @return true - запущен корректно, false - ошибка.
     */
    private static boolean startFFMpegProcess() {
        if (processVideo == null) {
            setVars();

            // Если файл видео уже есть - стираем.
            deleteFile(videoName);

            // Если файл аудио уже есть - стираем.
            if (isAudio) {
                deleteFile(audioName);
                // Создаём файл-канал для аудио.
                if (!isAudioFile) {
                    if (!createPipe(audioName)) {
                        // Ошибка при создании канала.
                        isAudio = false;
                        App.log("Ошибка при создании канала аудио! Аудио не обрабатывается!");
                    }
                }
            }

            // Если файл субтитров уже есть - стираем.
            if (isSub) {
                deleteFile(subName);
                // Создаём файл-канал для субтитров.
                if (!isSubFile) {
                    if (!createPipe(subName)) {
                        // Ошибка при создании канала.
                        isSub = false;
                        App.log("Ошибка при создании канала субтитров! Субтитры не обрабатываются!");
                    }
                }
            }

            ////////////////////////////////////////////////////////////////////
            // Для истчника аудио дефолтные параметры.
            String asrc = "-f g722 -acodec g722 -ar 8000 -ac 1 ";
            // Для истчника субтитров дефолтные параметры.
            String ssrc = "-f srt -scodec srt ";

            // Компилируем командную строку для ffmpeg.
            // Для видео.
            StringBuilder vcmd = new StringBuilder("ffmpeg ");
            // Оригинальный fps.
            String sfps = String.valueOf(fps);
            // Оригинальный размер кадра.
            Dimension d = fileInfo.frameFirst.getResolution();
            String size = "" + d.width + "x" + d.height;
            // Настройки источников...
            vcmd.append(" -r ").append(sfps).append(" -i - ");
            if (isAudio && !isAudioFile) { // Отдельный файл.
                vcmd.append(asrc).append(" -i ").append(audioName).append(" ");
            }
            if (isSub && !isSubFile) {
                vcmd.append(ssrc).append(" -i ").append(subName).append(" ");
            }
            // Настройки приёмника.
            vcmd.append(App.destVideoOptions.replace("{origfps}", sfps).replace("{origsize}", size));
            vcmd.append(" ");
            if (isAudio && !isAudioFile) { // Поток.
                vcmd.append(App.destAudioOptions).append(" ");
            }
            if (isSub && !isSubFile) { // Поток.
                vcmd.append(App.destSubOptions).append(" ");
            }
            vcmd.append(App.destVideoName);

            ////////////////////////////////////////////////////////////////////
            // Для аудио.
            StringBuilder acmd = new StringBuilder("ffmpeg ");
            if (isAudio) {
                acmd.append(asrc).append(" -i - ");
                acmd.append(App.destAudioOptions).append(" ");
                acmd.append(audioName);
            }

            ////////////////////////////////////////////////////////////////////
            // Для субтитров.
            StringBuilder scmd = new StringBuilder("ffmpeg ");
            if (isSub && isSubFile) {
                scmd.append(ssrc).append(" -i - ");
                scmd.append(App.destSubOptions).append(" ");
                scmd.append(subName);
            }

            if (!App.isDebug) {
                App.log("FFMpeg Video = " + vcmd.toString());
                App.log("FFMpeg Audio = " + acmd.toString());
                App.log("FFMpeg Sub   = " + scmd.toString());
            }

            // Стартуем процесс обработки видео.
            try {
                processVideo = Runtime.getRuntime().exec(vcmd.toString());
                processVideoOut = processVideo.getOutputStream();
            } catch (IOException ex) {
                Err.log(ex);
                cancelProcess();
                App.log("Ошибка запуска FFMpeg-video!");
                return false;
            }

            // Стартуем процесс обработки аудио.
            if (isAudio) {
                try {
                    processAudio = Runtime.getRuntime().exec(acmd.toString());
                    processAudioOut = processAudio.getOutputStream();
                } catch (IOException ex) {
                    Err.log(ex);
                    cancelProcess();
                    App.log("Ошибка запуска FFMpeg-audio!");
                    return false;
                }
            }

            // Стартуем процесс обработки субтитров.
            if (isSub) {
                try {
                    processSubOut = new PrintStream(new FileOutputStream(subName, true));
                } catch (IOException ex) {
                    Err.log(ex);
                    cancelProcess();
                    App.log("Ошибка запуска FFMpeg-sub!");
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Принудительное сворачивание процессов и удаление каналов при ошибке.
     */
    private static void cancelProcess() {
        if (processVideo != null) {
            processVideo.destroy();
            processVideo = null;
            processVideoOut = null;
        }
        if (processAudio != null) {
            processAudio.destroy();
            processAudio = null;
            processAudioOut = null;
        }
        if (processSubOut != null) {
            processSubOut.close();
            processSubOut = null;
        }
        if (isAudio && (!isAudioFile || isAudioTemp)) {
            deleteFile(audioName);
        }
        if (isSub && (!isSubFile || isSubTemp)) {
            deleteFile(subName);
        }
    }

    /**
     * Завершение процесса FFMpeg. Сначала закрывает принимающий поток, потом
     * ожидает завершения процесса и при истечении таймаута, если процесс не
     * завершился - завершает его принудительно.
     * @param p Процесс.
     * @param title Название процесса.
     * @param timeout Таймаут ожидания выхода.
     * @return true - закрылся сам, false - закрыт принудительно.
     */
    @SuppressWarnings("SleepWhileInLoop")
    private static boolean stopProcess(Process p, String title, int timeout) {
        if (p == null) {
            return true;
        }
        try {
            OutputStream out = p.getOutputStream();
            out.flush();
            out.close();
        } catch (IOException ex) {
            Err.log(ex);
        }
        while (true) {
            long time = new Date().getTime();
            // Ожидание выхода и запрос на форсированное снятие процесса!
            while (new Date().getTime() < time + timeout) {
                try {
                    p.exitValue();
                    return true;
                } catch (IllegalThreadStateException e) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ex) {
                    }
                }
            }
            Object[] options = {"Завершить", "Ожидать завершения"};
            int n = JOptionPane.showOptionDialog(App.mainFrame,
                    "Завершить процесс '" + title + "' принудительно или ожидать ещё " + timeout + " мсек.?",
                    "Подтверждение", JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE, null, options,
                    options[1]);
            if (n == 0) {
                p.destroy();
                App.log("Процесс '" + title + "' завершен принудительно!");
                return false;
            }
        }
    }

    /**
     * Завершение процессов FFMpeg. Удаление каналов.
     */
    private static void stopFFMpegProcess() {
        // Остановка процессов.
        stopProcess(processVideo, "FFMpeg-video", 10000);
        stopProcess(processAudio, "FFMpeg-audio", 10000);
        if (isSub && processSubOut != null) {
            processSubOut.close();
        }
        processVideo = null;
        processVideoOut = null;
        processAudio = null;
        processAudioOut = null;
        processSubOut = null;
        // Удаление каналов.
        if (isAudio && (!isAudioFile || isAudioTemp)) {
            deleteFile(audioName);
        }
        if (isSub && (!isSubFile || isSubTemp)) {
            deleteFile(subName);
        }
    }

    private static void processFile(FileInfo fileinfo) throws SourceException, FFMpegException {
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

        if (processVideo == null) {
            App.log("Запуск процесса кодирования...");
            if (!startFFMpegProcess()) {
                App.log("Ошибка запуска процесса кодирования!");
                if (in != null) {
                    in.closeSafe();
                }
                throw new FFMpegException("FFMpeg process start fail!");
            }
            App.log("Запущен процесс кодирования.");
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
            App.log("File:" + fileInfo.fileName + " start=" + pos + " end=" + endpos + " size=" + fileInfo.fileSize);

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
                                + " step=" + (time - timeMax));
                        // Отбрасываем кадры, которые ранее последнего записанного кадра 
                        // (направление времени только на увеличение, а т.к. 
                        // дискретность времени в DVR-секунды, то неравенство не строгое!)
                        if (timeMax == -1 || time >= timeMax) {
                            // Если не было обработанных кадров - начинаем только с ключевого,
                            // если были - включаем любые.
                            if (f.isMain || frameProcessCount > 0) {

                                in.read(baFrame, f.videoSize + f.audioSize);
                                // Пишем видео в поток.
                                processVideoOut.write(baFrame, 0, f.videoSize);
                                // Пишем аудио в поток.
                                if (isAudio) {
                                    processAudioOut.write(baFrame, f.videoSize, f.audioSize);
                                }
                                // Пишем субтитры в поток.
                                if (isSub) {
                                    writeSub(f.time, false); // Пишет при необходимости инфу.
                                }
                                frame = f;
                                frameProcessCount++;
                                videoProcessSize += f.videoSize;
                                audioProcessSize += isAudio ? f.audioSize : 0;
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
            // Завершаем открытые субтитры.
            if (isSub) {
                writeSub(new Date(), true);
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
    private static Date subTimeLast = null; // Время последнего незаписанного субтитра.
    private static long subFrameLast; // Номер кадра последнего незаписанного субтитра.
    private static long subCount; // Кол-во записанных субтитров.

    static String getFTime(long frames, int shift) {
        // Вычисляем время в файле для пред.фрейма.
        long msec1 = (long) ((double) frames * 1000 / fps) + shift;
        long h1 = msec1 / 3600 / 1000;
        msec1 -= h1 * 3600 * 1000;
        long m1 = msec1 / 60 / 1000;
        msec1 -= m1 * 60 * 1000;
        long s1 = msec1 / 1000;
        msec1 -= s1 * 1000;
        return String.format("%1$02d:%2$02d:%3$02d,%4$03d", h1, m1, s1, msec1);
    }

    /**
     * Если для текущего фрейма необходимо - формирует субтитры согласно 
     * настройкам и пишет их в поток.
     */
    private static void writeSub(Date dt, boolean isend) {
        //
        if (isSub) {
            if (subTimeLast == null) { // Начало работы - установка начальных указателей.
                subTimeLast = dt;
                subFrameLast = 0;
                subCount = 0;
            }
            long t1 = dt.getTime() / 1000;
            long t2 = subTimeLast.getTime() / 1000;
            if (t1 != t2 || isend) { // Отслеживаем смену секунды (как минимум).
                subCount++;
                processSubOut.printf("%d\n%s --> %s\n",
                        subCount, getFTime(subFrameLast, 0), getFTime(frameProcessCount, -1));
                processSubOut.printf("%1$td.%1$tm.%1$tY %1$tH:%1$tM:%1$tS\n", subTimeLast);
                long nn = frameProcessCount - subFrameLast;
                processSubOut.printf("st=%1$tM:%1$tS %2$tM:%2$tS sf=%3$d %4$d [%5$d]\n\n",
                        subTimeLast, dt, subCount, frameProcessCount, nn);
                if (nn != fps) {
                    App.log(String.format("st=%1$tM:%1$tS %2$tM:%2$tS sf=%3$d %4$d [%5$d]",
                            subTimeLast, dt, subCount, frameProcessCount, nn));
                }
                subTimeLast = dt;
                subFrameLast = frameProcessCount;
            }
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
}
