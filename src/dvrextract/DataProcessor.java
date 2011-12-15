/*
 * Copyright (c) 2011, Aleksey Nikolaevich Dokshin. All right reserved.
 * Contacts: dant.it@gmail.com, dokshin@list.ru.
 */
package dvrextract;

import dvrextract.FFMpeg.Cmd;
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
 * @author Докшин Алексей Николаевич <dant.it@gmail.com>
 */
@SuppressWarnings("SleepWhileInLoop")
public class DataProcessor {

    ////////////////////////////////////////////////////////////////////////////
    /**
     * Основной процесс ffmpeg - обработка видео.
     */
    private static Process processVideo;
    /**
     * Процесс для обработки аудио в отдельный файл.
     */
    private static Process processAudio;
    /**
     * Выходной поток для генерации субтитров в файл.
     */
    private static PrintStream processSubOut;
    /**
     * Процесс для окончательной сборки видеофайла.
     */
    private static Process processMake;
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Информация о текущей камере.
     */
    private static CamInfo camInfo;
    /**
     * Информация о текущем обрабатываемом файле.
     */
    private static FileInfo fileInfo;
    /**
     * Последний обработанный фрейм.
     */
    private static Frame frame;
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Кол-во распарсеных кадров (кол-во вызовов парсера).
     */
    public static long frameParsedCount;
    /**
     * Кол-во обработанных кадров (сохранённых).
     */
    public static long frameProcessCount;
    /**
     * Размер обработанных данных видео.
     */
    public static long videoProcessSize;
    /**
     * Размер обработанных данных аудио.
     */
    public static long audioProcessSize;
    /**
     * Минимальное время среди обработанных кадров.
     */
    public static long timeMin;
    /**
     * Максимальное время среди обработанных кадров.
     */
    public static long timeMax;
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Текстовые ресурсы для интерфейса.
     */
    public static String x_ProcessSource, x_ProcessFile, x_ProcessEnd,
            x_ProcessSourceEnd, x_FinalMakeStart, x_ErrorFinalMakeStart,
            x_FinalMakeStarted, x_FFMpegVideoInputNotFound,
            x_FFMpegProcessVideoInputFail, x_UserPorcessCancel, x_ErrorMakeIO,
            x_FinalMakeEnd, x_WrongOutVideoFile, x_WrongOutAudioFile,
            x_WrongOutSubFile, x_ErrorFFMpegStart, x_Delete, x_LeaveAsIs, x_WhatDoTemp,
            x_Confirmation, x_Finish, x_WaitFinish, x_FinishProcess, x_FinishedProcess,
            x_CoderStarting, x_CoderStarted;

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
        camInfo = App.Source.getCamInfo(App.Source.getSelectedCam());

        String msg = x_ProcessSource;
        App.log(msg);
        App.gui.setProgressInfo(msg);
        App.gui.startProgress(0, 100);

        try {
            for (int i = 0; i < camInfo.files.size(); i++) {
                if (Task.isTerminate()) {
                    break;
                }
                FileInfo fi = camInfo.files.get(i);

                msg = String.format(x_ProcessFile, i + 1, camInfo.files.size());
                App.log(msg + ": " + fi.fileName);
                App.gui.setProgressInfo(msg);
                App.gui.setProgressText(fi.fileName);

                // Выбираем только те файлы, промежутки которых попадают в 
                if (!fi.frameFirst.time.after(App.Dest.getTimeEnd())
                        && !fi.frameLast.time.before(App.Dest.getTimeStart())
                        && (timeMax == -1 || fi.frameLast.time.getTime() >= timeMax)) {
                    try {
                        processFile(fi);
                    } catch (SourceException ex) {
                        App.log(ex.getMessage());
                        break;
                    }
                }
            }
            // Останов процессов FFMpeg.
            stopCoderProcess();
            App.log(x_ProcessEnd);

            // Завершающая сборка видео (если есть аудио или субтитры в поток).
            finalMake();
            // Удаление временных файлов.
            deleteTempFiles();

        } catch (FatalException ex) {
            Err.log(ex);
            App.log(ex.getMessage());
            cancelAllProcess();
        }
        App.gui.stopProgress();

        msg = x_ProcessSourceEnd;
        App.log(msg);
        App.gui.setProgressInfo(msg);
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
        deleteFile(App.Dest.getVideoName());

        // Построение команды для сборки.
        // Для контроля прогресса будем сами закидывать данные видео.
        Cmd vcmd = new Cmd("-f", "matroska", "-i", "-");
        if (isAudio && isAudioTemp) {
            vcmd.add("-f", "wav", "-i", audioName);
        }
        if (isSub && isSubTemp) {
            vcmd.add("-f", "srt", "-i", subName);
        }
        vcmd.add("-vcodec", "copy");
        if (isAudio && isAudioTemp) {
            vcmd.add("-acodec", "copy");
        }
        if (isSub && isSubTemp) {
            vcmd.add("-scodec", "copy");
        }
        vcmd.add(App.Dest.getVideoName());

        if (App.isDebug) {
            App.log("FFMpeg Make = " + vcmd.toString());
        }

        String msg = x_FinalMakeStart;
        App.log(msg);
        App.gui.setProgressInfo(msg);
        App.gui.startProgress(0, 100);

        try {
            processMake = startProcess(vcmd);
        } catch (IOException ex) {
            throw new FatalException(x_ErrorFinalMakeStart);
        }
        OutputStream makeOut = processMake.getOutputStream();

        msg = x_FinalMakeStarted;
        App.log(msg);
        App.gui.setProgressInfo(msg);

        InputFile in;
        try {
            in = new InputFile(videoName);
        } catch (FileNotFoundException ex) {
            throw new FatalException("FFMPEG-VIDEO: " + x_FFMpegVideoInputNotFound + " [" + videoName + "]");
        } catch (IOException ex) {
            throw new FatalException("FFMPEG-VIDEO: " + x_FFMpegProcessVideoInputFail);
        }

        try {
            long size = in.getSize();
            App.gui.startProgress(0, 500);
            App.gui.setProgressText(App.Dest.getVideoName());

            long readsize = 0;
            int n = 0;
            byte[] buf = new byte[1024 * 1024];
            while (readsize < size) {
                if (Task.isTerminate()) {
                    throw new FatalException(x_UserPorcessCancel);
                }
                int len = (int) Math.min(size - readsize, buf.length);
                in.read(buf, len);
                makeOut.write(buf, 0, len);
                readsize += len;
                // Прогрес.
                int nnew = (int) (readsize * 500 / size);
                if (nnew != n) {
                    App.gui.setProgress(n);
                    n = nnew;
                }
            }
            App.gui.setProgress(500);
        } catch (IOException ex) {
            Err.log(ex);
            in.closeSafe();
            throw new FatalException(x_ErrorMakeIO);
        }
        in.closeSafe();

        // Остановка процесса.
        stopProcess(processMake, "FFMpeg-make", 10000);
        processMake = null;

        App.log(x_FinalMakeEnd);
    }
    /**
     * Имена файлов для сохранения видео, аудио и субтитров.
     */
    private static String videoName, audioName, subName;
    /**
     * Флаги сохранения аудио и субтитров.
     */
    private static boolean isAudio, isSub;
    /**
     * Флаги временности файлов.
     */
    private static boolean isAudioTemp, isSubTemp;
    /**
     * ФПС на входе.
     */
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
     * Проверяем на валидность файл (на то, что его можно создать и открыть).
     * @return true - можно использовать файл для вывода, false - нельзя.
     */
    private static boolean allowOutFile(String name) {
        deleteFile(name);
        try {
            FileOutputStream fos = new FileOutputStream(name);
            fos.close();
            deleteFile(name);
            return true;
        } catch (Exception ex) {
        }
        deleteFile(name);
        return false;
    }

    /**
     * Старт процесса выполнения команды.
     * @param cmd Текст команды.
     * @return Процесс выполнения команды.
     * @throws IOException Ошибка выполнения команды.
     */
    private static Process startProcess(Cmd cmd) throws IOException {
        Process p = Runtime.getRuntime().exec(cmd.getArray());
        if (App.isWindows) {
            p.getErrorStream().close(); // Без этого не начинается процессинг в винде!!!
        }
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
        switch (App.Dest.getAudioType()) {
            case -1: // Без аудио.
                isAudio = false;
                break;
            case 0: // Отдельный файл.
                isAudio = true;
                isAudioTemp = false;
                audioName = App.Dest.getAudioName();
                break;
            case 1: // Поток (врем.файл + сборка).
                isAudio = true;
                isAudioTemp = true;
                audioName = App.Dest.getVideoName() + ".audio.wav";
                break;
        }

        // Субтитры.
        switch (App.Dest.getSubType()) {
            case -1: // Без аудио.
                isSub = false;
                break;
            case 0: // Отдельный файл.
                isSub = true;
                isSubTemp = false;
                subName = App.Dest.getSubName();
                break;
            case 1: // Поток (врем.файл + сборка).
                isSub = true;
                isSubTemp = true;
                subName = App.Dest.getVideoName() + ".sub.srt";
                break;
        }

        // Если есть временные файлы - значит будет послед.слив в одно видео, 
        // значит сначала кодируем видео во временный файл.
        videoName = App.Dest.getVideoName()
                + ((isAudio && isAudioTemp) || (isSub && isSubTemp) ? ".video.mkv" : "");
        fps = fileInfo.frameFirst.fps;

        ////////////////////////////////////////////////////////////////////
        // Проверка имён файлов на возможность их содания и записи в них.
        // Существующие файлы стираются!

        // Проверка файла видео результирующего!
        if ((isAudio && isAudioTemp) || (isSub && isSubTemp)) {
            if (!allowOutFile(App.Dest.getVideoName())) {
                throw new FatalException(x_WrongOutVideoFile + " [" + App.Dest.getVideoName() + "]");
            }
        }

        // Проверка файла видео.
        if (!allowOutFile(videoName)) {
            throw new FatalException(x_WrongOutVideoFile + " FFMPEG-VIDEO [" + videoName + "]");
        }

        // Проверка файла аудио.
        if (isAudio) {
            if (!allowOutFile(audioName)) {
                throw new FatalException(x_WrongOutAudioFile + " FFMPEG-AUDIO [" + audioName + "]");
            }
        }
        // Проверка файла субтитров.
        if (isSub) {
            if (!allowOutFile(subName)) {
                throw new FatalException(x_WrongOutSubFile + " FFMPEG-SUB [" + subName + "]");
            }
        }

        ////////////////////////////////////////////////////////////////////
        // Компилируем командную строку для ffmpeg.
        // Для видео.
        // Оригинальный fps.
        String sfps = String.valueOf(fps);
        // Оригинальный размер кадра.
        Dimension d = fileInfo.frameFirst.getResolution();
        String ssize = "" + d.width + "x" + d.height;
        // Настройки приёмника.
        Cmd vcmd = new Cmd("-r", sfps, "-i", "-");
        vcmd.add(App.Dest.getVideoOptions()).add(videoName);
        vcmd.replaceOrigs(sfps, ssize);
        //

        ////////////////////////////////////////////////////////////////////
        // Для аудио.
        Cmd acmd = new Cmd();
        if (isAudio) {
            acmd.add("-f", "s16le", "-ar", "8000", "-i", "-");
            acmd.add(App.Dest.getAudioOptions()).add(audioName);
        }

        if (App.isDebug) {
            App.log("FFMpeg Video = " + vcmd.toString());
            App.log("FFMpeg Audio = " + acmd.toString());
        }

        ////////////////////////////////////////////////////////////////////
        // Стартуем процесс обработки видео.
        try {
            processVideo = startProcess(vcmd);
        } catch (IOException ex) {
            throw new FatalException("FFMPEG-VIDEO: " + x_ErrorFFMpegStart);
        }

        // Стартуем процесс обработки аудио.
        if (isAudio) {
            try {
                processAudio = startProcess(acmd);
            } catch (IOException ex) {
                throw new FatalException("FFMPEG-AUDIO: " + x_ErrorFFMpegStart);
            }
        }

        // Стартуем "процесс" обработки субтитров.
        if (isSub) {
            try {
                processSubOut = new PrintStream(new FileOutputStream(subName, true));
            } catch (IOException ex) {
                throw new FatalException("FFMPEG-SUB: " + x_ErrorFFMpegStart);
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
        }
        if (processAudio != null) {
            processAudio.destroy();
            processAudio = null;
        }
        if (processSubOut != null) {
            processSubOut.close();
            processSubOut = null;
        }
        if (processMake != null) {
            processMake.destroy();
            processMake = null;
        }
        Object[] options = {x_Delete, x_LeaveAsIs};
        int n = JOptionPane.showOptionDialog(App.gui,
                x_WhatDoTemp,
                x_Confirmation, JOptionPane.YES_NO_OPTION,
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
            Object[] options = {x_Finish, x_WaitFinish};
            int n = JOptionPane.showOptionDialog(App.gui,
                    String.format(x_FinishProcess, title, timeout),
                    x_Confirmation, JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE, null, options,
                    options[1]);
            if (n == 0) {
                p.destroy();
                App.log(String.format(x_FinishedProcess, title));
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
        processAudio = null;
        processSubOut = null;
    }

    /**
     * Обработка файла источника.
     * @param fileinfo Информация о файле.
     * @throws dvrextract.DataProcessor.SourceException Ошибка в источнике.
     * @throws dvrextract.DataProcessor.FFMpegException Ошибка ffmpeg/крит.ошибка.
     */
    private static void processFile(FileInfo fileinfo) throws SourceException, FatalException {
        InputBufferedFile in = null;
        //InputFile in = null;
        try {
            in = new InputBufferedFile(fileinfo.fileName, 1000000, 100);
            //in = new InputFile(fileinfo.fileName);
        } catch (FileNotFoundException ex) {
            throw new SourceException("File not found = " + fileinfo.fileName);
        } catch (IOException ex) {
            throw new SourceException("File IO = " + fileinfo.fileName);
        }

        fileInfo = fileinfo; // Нужно для успешного старта FFMpeg (дефолтные фпс и размеры)

        if (processVideo == null) {
            App.log(x_CoderStarting);
            try {
                startFFMpegProcess();
            } catch (FatalException ex) {
                if (in != null) {
                    in.closeSafe();
                }
                throw ex;
            }
            App.log(x_CoderStarted);
        }

        try {
            App.gui.startProgress(0, 100);
            App.gui.setProgressText(fileInfo.fileName);

            OutputStream videoOut = processVideo.getOutputStream();
            OutputStream audioOut = isAudio ? processAudio.getOutputStream() : null;
            OutputFile rawAOut = isAudio ? new OutputFile(fileInfo.fileName + ".audio.raw") : null;

            int cam = App.Source.getSelectedCam();
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

            final byte[] outAudioFrame = new byte[ADPCM.MAX_PCMFRAME_SIZE];
            ADPCM decoder = new ADPCM(0x23, baFrame, outAudioFrame);
            int n = 0;
            long timeStart = App.Dest.getTimeStart().getTime();
            long timeEnd = App.Dest.getTimeEnd().getTime();

            // Идём по кадрам от начала к концу.
            for (; pos < endpos - frameSize; pos++) {
                if (Task.isTerminate()) {
                    throw new FatalException(x_UserPorcessCancel);
                }
                in.seek(pos);
                in.read(baFrame, frameSize);
                if (f.parseHeader(bbF, 0) == 0) {
                    // Берем только фреймы выбранной камеры (актуально для EXE файла).
                    if (f.camNumber == cam && f.tm == 0x4E) { // TODO: Trial remove.
                        // Если это не ключевой кадр и в выводе пусто - пропускаем.
                        // TODO: Возможно будет нужно доработать логику! 
                        // Т.к. может случится так, что первый кадр файла не ключевой,
                        // а продолжение предыдущего, но предыдущего нет, а есть 
                        // предпредыдущий - будет неверным добавлять этот кадр.
                        long time = f.time.getTime();
                        // Отбрасываем кадры, которые ранее последнего записанного кадра 
                        // (направление времени только на увеличение, а т.к. 
                        // дискретность времени в DVR-секунды, то неравенство не строгое!)
                        if ((timeMax == -1 || time >= timeMax)
                                // Ограничение по времени.
                                && (time >= timeStart && time <= timeEnd)
                                && ((time >> 32) - 0x133 < 2)) { // TODO: Trial remove.

                            // Если не было обработанных кадров - начинаем только с ключевого,
                            // если были - включаем любые.
                            if (f.isMain || frameProcessCount > 0) {

                                in.read(baFrame, f.videoSize + f.audioSize);
                                // Пишем видео в поток.
                                videoOut.write(baFrame, 0, f.videoSize);
                                // Пишем аудио в поток.
                                if (isAudio) {
                                    rawAOut.write(baFrame, f.videoSize, f.audioSize);
                                    for (int np = 0; np < f.audioSize;) {
                                        int res = decoder.HI_VOICE_Decode(f.videoSize + np, 0);
                                        if (res == 0) {
                                            audioOut.write(outAudioFrame, 0, decoder.getOutShift());
                                            np += decoder.getInShift();
                                        } else {
                                            np += 168; // Пропускаем кадр, размер по умолчанию.
                                            App.log("ErrAudio=" + res);
                                        }
                                    }
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
                    App.gui.setProgress(n);
                    n = nnew;
                }
            }
            App.gui.setProgress(100);

            // Завершаем открытые субтитры.
            if (isSub) {
                writeSub(new Date(), true);
            }
            rawAOut.closeSafe();

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
    /**
     * Время последнего незаписанного субтитра.
     */
    private static Date subTimeLast = null;
    /**
     * Номер кадра последнего незаписанного субтитра.
     */
    private static long subFrameLast;
    /**
     * Кол-во записанных субтитров.
     */
    private static long subCount;

    /**
     * Возвращает строку ВРЕМЕНИ в файле для предыдущего указанному номеру 
     * кадра с заданным сдвигом в миллисекундах.
     * Используется для записи в файл длительности титров.
     * @param frames Номер кадра (1-первый).
     * @param shift Сдвиг в миллисекундах (может быть отрицательным).
     * @return Строка времени.
     */
    private static String getFTime(long frames, int shift) {
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

    /**
     * Исключение при ошибке запуска ffmpeg или при фатальных ошибках требующих
     * остановить обработку.
     */
    public final static class FatalException extends Exception {

        /**
         * Конструктор.
         * @param msg Текст сообщения об ошибке.
         */
        public FatalException(String msg) {
            super(msg);
        }
    }

    /**
     * Исключение при ошибках источника.
     */
    public final static class SourceException extends Exception {

        /**
         * Конструктор.
         * @param msg Текст сообщения об ошибке.
         */
        public SourceException(String msg) {
            super(msg);
        }
    }
}