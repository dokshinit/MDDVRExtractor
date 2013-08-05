/*
 * Copyright (c) 2011-2013, Aleksey Nikolaevich Dokshin. All right reserved.
 * Contacts: dant.it@gmail.com, dokshin@list.ru.
 */
package mddvrextractor;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import mddvrextractor.util.NumberTools;
import mddvrextractor.xfsengine.XFS.XFSException;

/**
 * Процесс вычисления объёмов данных и времени.
 *
 * @author Докшин Алексей Николаевич <dant.it@gmail.com>
 */
@SuppressWarnings("SleepWhileInLoop")
public class DataCalculator {

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
    public static double frameProcessDuration;
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
    /**
     * Флаг успшного выполнения операции.
     */
    public static String errorMessage;
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Текстовые ресурсы для интерфейса.
     */
    public static String x_CalcEnd, x_CalcStart, x_Results, x_Cam, x_Duration,
            x_VideoSize, x_AudioSize, x_Title, x_Note, x_Close;

    /**
     * Обработка данных.
     */
    public static void process() {
        frameParsedCount = 0;
        frameProcessCount = 0;
        frameProcessDuration = 0;
        videoProcessSize = 0;
        audioProcessSize = 0;
        errorMessage = null;
        timeMin = -1;
        timeMax = -1;
        // Проверка и обработка в хронологическом порядке списка файлов.
        camInfo = App.Source.getCamInfo(App.Source.getSelectedCam());

        String msg = x_CalcStart;
        App.log(msg);
        App.gui.setProgressInfo(msg);
        App.gui.startProgress(0, 100);

        try {
            for (int i = 0; i < camInfo.files.size(); i++) {
                if (Task.isTerminate()) {
                    break;
                }
                FileInfo fi = camInfo.files.get(i);

                msg = String.format(DataProcessor.x_ProcessFile, i + 1, camInfo.files.size());
                App.log(msg + ": " + fi.fileName);
                App.gui.setProgressInfo(msg);
                App.gui.setProgressText(fi.fileName.toString());

                // Выбираем только те файлы, промежутки которых попадают в 
                if (!fi.frameFirst.time.after(App.Dest.getTimeEnd())
                        && !fi.frameLast.time.before(App.Dest.getTimeStart())
                        && (timeMax == -1 || fi.frameLast.time.getTime() >= timeMax)) {
                    try {
                        processFile(fi);
                    } catch (SourceException ex) {
                        App.log(ex.getMessage());
                        errorMessage = ex.getMessage();
                        break;
                    }
                }
            }

        } catch (FatalException ex) {
            Err.log(ex);
            App.log(ex.getMessage());
            errorMessage = ex.getMessage();
        }
        App.gui.stopProgress();

        App.log(x_Results + ":");
        App.log(x_Duration + " = " + getDurationAsText());
        App.log(x_VideoSize + " = " + getVideoSizeAsText());
        App.log(x_AudioSize + " = " + getAudioSizeAsText());

        msg = x_CalcEnd;
        App.log(msg);
        App.gui.setProgressInfo(msg);
    }

    /**
     * Обработка файла источника.
     *
     * @param fileinfo Информация о файле.
     * @throws dvrextract.DataProcessor.SourceException Ошибка в источнике.
     * @throws dvrextract.DataProcessor.FFMpegException Ошибка
     * ffmpeg/крит.ошибка.
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
        } catch (XFSException ex) {
            throw new SourceException("XFS Error = " + fileinfo.fileName);
        }

        fileInfo = fileinfo;

        try {
            App.gui.startProgress(0, 100);
            App.gui.setProgressText(fileInfo.fileName.toString());

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

            // Буфер чтения и парсинга данных.
            final byte[] baFrame = new byte[1000];
            final ByteBuffer bbF = ByteBuffer.wrap(baFrame);
            bbF.order(ByteOrder.LITTLE_ENDIAN);

            int n = 0;
            long timeStart = App.Dest.getTimeStart().getTime();
            long timeEnd = App.Dest.getTimeEnd().getTime();
            long countDur = 0;
            int fpsDur = 0;

            // Идём по кадрам от начала к концу.
            for (; pos < endpos - frameSize; pos++) {
                if (Task.isTerminate()) {
                    throw new FatalException(DataProcessor.x_UserPorcessCancel);
                }
                in.seek(pos);
                in.read(baFrame, frameSize);
                if (f.parseHeader(bbF, 0) == 0) {
                    // Берем только фреймы выбранной камеры (актуально для EXE файла).
                    if (f.camNumber == cam) { // && f.tm == 0x4E) { // TODO: Trial injection (commented).
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
                                && (time >= timeStart && time <= timeEnd)) {
                            //&& ((time >> 32) - 0x133 < 2)) { // TODO: Trial injection (commented).

                            // Если не было обработанных кадров - начинаем только с ключевого,
                            // если были - включаем любые.
                            if (f.isMain || frameProcessCount > 0) {

                                // Включаем в расчёт!

                                if (fpsDur == 0) {
                                    fpsDur = f.fps;
                                }
                                // Если изменился fps - записываем накопленную длительность.
                                if (f.fps != fpsDur) {
                                    frameProcessDuration += countDur / fpsDur;
                                    countDur = 0;
                                    fpsDur = f.fps;
                                }
                                countDur++;

                                frameProcessCount++;
                                //frameProcessDuration += 1.0 / f.fps;
                                videoProcessSize += f.videoSize;
                                audioProcessSize += f.audioSize;
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
            if (countDur > 0) { // Записываем накопленную длительность.
                frameProcessDuration += countDur / fpsDur;
            }
            App.gui.setProgress(100);

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

    public static String getDurationAsText() {
        return App.timeToString((long) (frameProcessDuration * 1000));
    }

    public static String getVideoSizeAsText() {
        return NumberTools.doubleToFormatString((double) videoProcessSize, NumberTools.format0, "", "0")
                + " " + App.x_Bytes;
    }

    public static String getAudioSizeAsText() {
        return NumberTools.doubleToFormatString((double) audioProcessSize, NumberTools.format0, "", "0")
                + " " + App.x_Bytes;
    }
}