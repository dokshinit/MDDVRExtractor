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
 * TODO: Сделать при ошибках - развертку и вывод err потока ffmpeg.
 * 
 * @author lex
 */
@SuppressWarnings("SleepWhileInLoop")
public class DataProcessor {

    ////////////////////////////////////////////////////////////////////////////
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
        subTimeLast = null; // Для субтитров как маркер начала заново.
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
        App.mainFrame.startProgress(0, 100);

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


        } catch (FatalException ex) {
            Err.log(ex);
            App.log(ex.getMessage());
            cancelAllProcess();
        }
        App.mainFrame.stopProgress();

        msg = "Обработка источника завершена.";
        App.log(msg);
        App.mainFrame.setProgressInfo(msg);
    }

    /**
     * Финальная сборка видеофайла.
     * @throws dvrextract.DataProcessor.FFMpegException Ошибка.
     */
    private static void finalMake() throws FatalException {
        // Если нет временных файлов - значит не нужна сборка.
        if (!(isAudio && isAudioTemp) && !(isSub && isSubTemp)) {
            return;
        }
        if (frameProcessCount == 0) {
            return;
        }
        // Если файл видео уже есть - стираем.
        deleteFile(App.destVideoName);

        // Построение команды для сборки.
        StringBuilder vcmd = new StringBuilder("ffmpeg ");
        // Для контроля прогресса будем сами закидывать данные видео.
        vcmd.append("-f matroska -i - ");
        if (isAudio && isAudioTemp) {
            vcmd.append("-f wav -i ").append(audioName).append(" ");
        }
        if (isSub && isSubTemp) {
            vcmd.append("-f srt -i ").append(subName).append(" ");
        }
        vcmd.append("-vcodec copy ");
        if (isAudio && isAudioTemp) {
            vcmd.append("-acodec copy ");
        }
        if (isSub && isSubTemp) {
            vcmd.append("-scodec copy ");
        }
        vcmd.append(App.destVideoName);

        if (App.isDebug) {
            App.log("FFMpeg Make = " + vcmd.toString());
        }

        String msg = "Запуск процесса сборки...";
        App.log(msg);
        App.mainFrame.setProgressInfo(msg);
        App.mainFrame.startProgress(0, 100);

        try {
            processMake = startProcess(vcmd.toString());
            processMakeOut = processMake.getOutputStream();
        } catch (IOException ex) {
            throw new FatalException("Ошибка запуска процесса сборки!");
        }

        msg = "Запущен процесс сборки.";
        App.log(msg);
        App.mainFrame.setProgressInfo(msg);

        InputFile in;
        try {
            in = new InputFile(videoName);
        } catch (FileNotFoundException ex) {
            throw new FatalException("FFMpeg process video input not found!");
        } catch (IOException ex) {
            throw new FatalException("FFMpeg process video input fail!");
        }

        try {
            long size = in.getSize();
            App.mainFrame.startProgress(0, 500);
            App.mainFrame.setProgressText(App.destVideoName);

            long readsize = 0;
            int n = 0;
            byte[] buf = new byte[1024 * 1024];
            while (readsize < size) {
                if (Task.isTerminate()) {
                    throw new FatalException("Прерывание процесса пользователем!");
                }
                int len = (int) Math.min(size - readsize, buf.length);
                in.read(buf, len);
                processMakeOut.write(buf, 0, len);
                readsize += len;
                // Прогрес.
                int nnew = (int) (readsize * 500 / size);
                if (nnew != n) {
                    App.mainFrame.setProgress(n);
                    n = nnew;
                }
            }
            App.mainFrame.setProgress(500);
        } catch (IOException ex) {
            Err.log(ex);
            in.closeSafe();
            throw new FatalException("Ошибка IO при сборке!");
        }
        in.closeSafe();

        // Остановка процесса.
        stopProcess(processMake, "FFMpeg-make", 10000);
        processMake = null;
        processMakeOut = null;

        App.log("Процесс сборки завершён.");
    }
    // Имена файлов для сохранения аудио и субтитров.
    private static String videoName, audioName, subName;
    // Флаги сохранения аудио и субтитров.
    private static boolean isAudio, isSub;
    // Флаги временности файлов.
    private static boolean isAudioTemp, isSubTemp;
    // ФПС на входе.
    private static int fps;

    /**
     * Удаление файла.
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

    /**
     * Старт процесса выполнения команды.
     * @param cmd Текст команды.
     * @return Процесс выполнения команды.
     * @throws IOException Ошибка выполнения команды.
     */
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
     * @exception FatalException Фатальная ошибка.
     */
    private static void startFFMpegProcess() throws FatalException {
        if (processVideo != null) {
            return;
        }

        ////////////////////////////////////////////////////////////////////
        // Установка переменных.

        // Аудио.
        isAudio = App.destAudioType == -1 ? false : true;
        if (App.destAudioType == 0) { // Отдельный файл.
            isAudioTemp = false;
            audioName = App.destAudioName;
        } else if (App.destAudioType == 1) { // Поток (врем.файл + сборка).
            isAudioTemp = true;
            audioName = App.destVideoName + ".audio.wav";
        }

        // Субтитры.
        isSub = App.destSubType == -1 ? false : true;
        if (App.destSubType == 0) { // Отдельный файл.
            isSubTemp = false;
            subName = App.destSubName;
        } else if (App.destSubType == 1) { // Поток (врем.файл + сборка).
            isSubTemp = true;
            subName = App.destVideoName + ".sub.srt";
        }

        // Если есть временные файлы - значит будет послед.слив в одно видео, 
        // значит сначала кодируем видео во временный файл.
        videoName = App.destVideoName + ((isAudio && isAudioTemp) || (isSub && isSubTemp) ? ".video.mkv" : "");
        fps = fileInfo.frameFirst.fps;

        ////////////////////////////////////////////////////////////////////
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
        vcmd.append(" ").append(videoName);

        ////////////////////////////////////////////////////////////////////
        // Для аудио.
        StringBuilder acmd = new StringBuilder("ffmpeg ");
        if (isAudio) {
            acmd.append("-f g722 -acodec g722 -ar 8000 -ac 1 -i - ");
            acmd.append(App.destAudioOptions).append(" ");
            acmd.append(audioName);
        }

        if (App.isDebug) {
            App.log("FFMpeg Video = " + vcmd.toString());
            App.log("FFMpeg Audio = " + acmd.toString());
        }

        ////////////////////////////////////////////////////////////////////
        // Стартуем процесс обработки видео.
        try {
            processVideo = startProcess(vcmd.toString());
            processVideoOut = processVideo.getOutputStream();
        } catch (IOException ex) {
            throw new FatalException("Ошибка запуска FFMpeg-video!");
        }

        // Стартуем процесс обработки аудио.
        if (isAudio) {
            try {
                processAudio = startProcess(acmd.toString());
                processAudioOut = processAudio.getOutputStream();
            } catch (IOException ex) {
                throw new FatalException("Ошибка запуска FFMpeg-audio!");
            }
        }

        // Стартуем "процесс" обработки субтитров.
        if (isSub) {
            try {
                processSubOut = new PrintStream(new FileOutputStream(subName, true));
            } catch (IOException ex) {
                throw new FatalException("Ошибка запуска FFMpeg-sub!");
            }
        }
    }

    /**
     * Удаление временных файлов.
     */
    private static void deleteTempFiles() {
        if ((isAudio && isAudioTemp) || (isSub && isSubTemp)) {
            deleteFile(videoName);
        }
        if (isAudio && isAudioTemp) {
            deleteFile(audioName);
        }
        if (isSub && isSubTemp) {
            deleteFile(subName);
        }
    }

    /**
     * Принудительное сворачивание процессов и удаление врем.файлов при ошибке.
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
        Object[] options = {"Удалить", "Оставить как есть"};
        int n = JOptionPane.showOptionDialog(App.mainFrame,
                "Что делать с временными файлами?",
                "Подтверждение", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, options,
                options[0]);
        if (n == 0) {
            deleteTempFiles();
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

    /**
     * Обработка файла источника.
     * @param fileinfo Информация о файле.
     * @throws dvrextract.DataProcessor.SourceException Ошибка в источнике.
     * @throws dvrextract.DataProcessor.FFMpegException Ошибка ffmpeg/крит.ошибка.
     */
    private static void processFile(FileInfo fileinfo) throws SourceException, FatalException {
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
            try {
                startFFMpegProcess();
            } catch (FatalException ex) {
                if (in != null) {
                    in.closeSafe();
                }
                throw ex;
            }
            App.log("Запущен процесс кодирования.");
        }

        try {
            App.mainFrame.startProgress(0, 100);

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
            if (App.isDebug) {
                App.log("File:" + fileInfo.fileName + " start=" + pos + " end=" + endpos + " size=" + fileInfo.fileSize);
            }
            // Буфер чтения и парсинга данных.
            final byte[] baFrame = new byte[1000000];
            final ByteBuffer bbF = ByteBuffer.wrap(baFrame);
            bbF.order(ByteOrder.LITTLE_ENDIAN);
            int n = 0;

            // Идём по кадрам от начала к концу.
            for (; pos < endpos - frameSize; pos++) {
                if (Task.isTerminate()) {
                    throw new FatalException("Прерывание процесса пользователем!");
                }
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
//                        App.log("Frame pos=" + pos + " cam=" + f.camNumber
//                                + " VSz=" + f.videoSize + " ASz=" + f.audioSize
//                                + " step=" + (time - timeMax));
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
                // Прогрес.
                int nnew = (int) (pos * 100 / (endpos - frameSize));
                if (nnew != n) {
                    App.mainFrame.setProgress(n);
                    n = nnew;
                }
            }
            App.mainFrame.setProgress(100);

            // Завершаем открытые субтитры.
            if (isSub) {
                writeSub(new Date(), true);
            }
            in.close();
            if (App.isDebug) {
                App.log("Frame parsed = " + frameParsedCount);
                App.log("Frame processed = " + frameProcessCount);
                App.log("Video size = " + videoProcessSize);
            }

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
    ////////////////////////////////////////////////////////////////////////////
    //
    //  СУБТИТРЫ
    //
    ////////////////////////////////////////////////////////////////////////////
    // Время последнего незаписанного субтитра.
    private static Date subTimeLast = null;
    // Номер кадра последнего незаписанного субтитра.
    private static long subFrameLast;
    // Кол-во записанных субтитров.
    private static long subCount;

    /**
     * Возвращает строку ВРЕМЕНИ в файле для предыдущего указанному номеру 
     * кадра с заданным сдвигом в миллисекундах.
     * Используется для записи в файл длительности титров.
     * @param frames Номер кадра (1-первый).
     * @param shift Сдвиг в миллисекундах (может быть отрицательным).
     * @return Строка времени.
     */
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
            if ((t1 != t2 || isend) && subFrameLast < frameProcessCount) { // Отслеживаем смену секунды (как минимум).
                subCount++;
                processSubOut.printf("%d\n%s --> %s\n",
                        subCount, getFTime(subFrameLast, 0), getFTime(frameProcessCount, -1));
                processSubOut.printf("%1$td.%1$tm.%1$tY %1$tH:%1$tM:%1$tS\n", subTimeLast);
                long nn = frameProcessCount - subFrameLast;
                if (App.isDebug) {
                    processSubOut.printf("st=%1$tM:%1$tS %2$tM:%2$tS sf=%3$d %4$d [%5$d]\n",
                            subTimeLast, dt, subCount, frameProcessCount, nn);
                    if (nn != fps) {
                        App.log(String.format("st=%1$tM:%1$tS %2$tM:%2$tS sf=%3$d %4$d [%5$d]",
                                subTimeLast, dt, subCount, frameProcessCount, nn));
                    }
                }
                processSubOut.printf("\n");
                subTimeLast = dt;
                subFrameLast = frameProcessCount;
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    //  СУБТИТРЫ
    //
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Исключение при ошибке запуска ffmpeg или при фатальных ошибках требующих
     * остановить обработку.
     */
    public final static class FatalException extends Exception {

        public FatalException(String msg) {
            super(msg);
        }
    }

    /**
     * Исключение при ошибках источника.
     */
    public final static class SourceException extends Exception {

        public SourceException(String msg) {
            super(msg);
        }
    }
}