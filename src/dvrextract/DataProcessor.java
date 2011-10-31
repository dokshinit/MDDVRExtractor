package dvrextract;

import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    // Процесс для окончательной сборки видеофайла.
    private static Process processMake;
    private static OutputStream processMakeOut;
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
            // Останов процессов FFMpeg.
            stopCoderProcess();
            App.log("Процесс кодирования завершён.");

            // Завершающая сборка видео (если есть аудио или субтитры в поток).
            finalMake();


        } catch (FFMpegException ex) {
            //App.log(ex.getMessage());
        }
        App.mainFrame.stopProgress();

        msg = "Обработка источника завершена.";
        App.log(msg);
        App.mainFrame.setProgressInfo(msg);
    }

    private static void finalMake() throws FFMpegException {
        if (!(isAudio && isAudioTemp) && !(isSub && isSubTemp)) {
            return;
        }
        StringBuilder vcmd = new StringBuilder("ffmpeg");
        vcmd.append("-f matroska -i - "); // Для контроля прогресса будем сами закидывать.
        if (isAudio && isAudioTemp) {
            vcmd.append("-i ").append(audioName);
        }
        if (isSub && isSubTemp) {
            vcmd.append("-i ").append(subName);
        }
        vcmd.append("-vcodec copy ");
        if (isAudio && isAudioTemp) {
            vcmd.append("-acodec copy ");
        }
        if (isSub && isSubTemp) {
            vcmd.append("-scodec copy ");
        }
        vcmd.append(App.destVideoName);

        App.log("Запуск процесса сборки...");
        try {
            processMake = startProcess(vcmd.toString());
            processMakeOut = processMake.getOutputStream();
        } catch (IOException ex) {
            Err.log(ex);
            cancelAllProcess();
            App.log("Ошибка запуска FFMpeg-video-make!");
            App.log("Ошибка запуска процесса сборки!");
            throw new FFMpegException("FFMpeg process start fail!");
        }

        App.log("Запущен процесс сборки.");
        InputFile in;
        try {
            in = new InputFile(videoName);
        } catch (FileNotFoundException ex) {
            Err.log(ex);
            throw new FFMpegException("FFMpeg process input not found!");
        } catch (IOException ex) {
            Err.log(ex);
            throw new FFMpegException("FFMpeg process input fail!");
        }

        try {
            long size = in.getSize();
            long readsize = 0;
            byte[] buf = new byte[1024*1024];
            while (readsize < size) {
                int len = (int)Math.min(size-readsize, buf.length);
                in.read(buf, len);
                processMakeOut.write(buf, 0, len);
                readsize += len;
            }
        } catch (IOException ex) {
            Err.log(ex);
        }
        in.closeSafe();
    }
    // Имена файлов для сохранения аудио и субтитров.
    private static String videoName, audioName, subName;
    // Флаги сохранения аудио и субтитров.
    private static boolean isAudio, isSub;
    // Флаги временности файлов.
    private static boolean isAudioTemp, isSubTemp;
    private static int fps;

    private static void setVars() {
        fps = fileInfo.frameFirst.fps;
        // Аудио.
        isAudio = App.destAudioType == -1 ? false : true;
        if (App.destAudioType == 0) { // Отдельный файл.
            isAudioTemp = false;
            audioName = App.destAudioName;
        } else if (App.destAudioType == 1) { // Поток.
            isAudioTemp = true;
            audioName = App.destVideoName + ".audio.wav";
        }
        // Субтитры.
        isSub = App.destSubType == -1 ? false : true;
        if (App.destSubType == 0) {
            isSubTemp = false;
            subName = App.destSubName;
        } else if (App.destSubType == 1) {
            // Отдельный промежуточный файл.
            isSubTemp = true;
            subName = App.destSubName + ".sub.srt";
        }
        // Если есть временные файлы - значит будет послед.слив в одно видео, 
        // значит сначала кодируем видео во временный файл.
        videoName = App.destVideoName + ((isAudio && isAudioTemp) || (isSub && isSubTemp) ? ".video.mkv" : "");
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

    private static Process startProcess(String cmd) throws IOException {
        Process p = Runtime.getRuntime().exec(cmd);
        int res = 0;
        try {
            res = p.exitValue();
        } catch (IllegalThreadStateException ex) {
        }
        return res == 0 ? p : null;
    }

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
            }
            // Если файл субтитров уже есть - стираем.
            if (isSub) {
                deleteFile(subName);
            }

            ////////////////////////////////////////////////////////////////////
            // Компилируем командную строку для ffmpeg.
            // Для видео.
            StringBuilder vcmd = new StringBuilder("ffmpeg ");
            // Оригинальный fps.
            String sfps = String.valueOf(fps);
            // Оригинальный размер кадра.
            Dimension d = fileInfo.frameFirst.getResolution();
            String size = "" + d.width + "x" + d.height;
            // Настройки приёмника.
            vcmd.append(" -r ").append(sfps).append(" -i - ");
            vcmd.append(App.destVideoOptions.replace("{origfps}", sfps).replace("{origsize}", size));
            vcmd.append(" ").append(App.destVideoName);

            ////////////////////////////////////////////////////////////////////
            // Для аудио.
            StringBuilder acmd = new StringBuilder("ffmpeg ");
            if (isAudio) {
                acmd.append("-f g722 -acodec g722 -ar 8000 -ac 1 -i - ");
                acmd.append(App.destAudioOptions).append(" ");
                acmd.append(audioName);
            }

            if (!App.isDebug) {
                App.log("FFMpeg Video = " + vcmd.toString());
                App.log("FFMpeg Audio = " + acmd.toString());
            }

            // Стартуем процесс обработки видео.
            try {
                processVideo = startProcess(vcmd.toString());
                processVideoOut = processVideo.getOutputStream();
            } catch (IOException ex) {
                Err.log(ex);
                cancelAllProcess();
                App.log("Ошибка запуска FFMpeg-video!");
                return false;
            }

            // Стартуем процесс обработки аудио.
            if (isAudio) {
                try {
                    processAudio = startProcess(acmd.toString());
                    processAudioOut = processAudio.getOutputStream();
                } catch (IOException ex) {
                    Err.log(ex);
                    cancelAllProcess();
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
                    cancelAllProcess();
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
    private static void cancelAllProcess() {
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
        if (processMake != null) {
            processMake.destroy();
            processMake = null;
            processMake = null;
        }
        deleteTempFiles();
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
    private static void stopCoderProcess() {
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
    }
    
    private static void deleteTempFiles() {
        if (isAudio && isAudioTemp) {
            deleteFile(audioName);
        }
        if (isSub && isSubTemp) {
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
