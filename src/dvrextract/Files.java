package dvrextract;

import dvrextract.LogTableModel.Type;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Осуществление действий с источником: сканирование, обработка.
 * -----------------------------------------------------------------------------
 * ФОРМАТ ОПИСАТЕЛЯ EXE ФАЙЛА:
 * <конец данных>
 * int ?; =2
 * int ?; =1
 * int[16] ?;            // (=0 - если камера есть, =-1 - если нет)
 * ---
 * long[16][101] offset; // Смещение в файле до опорных кадров с шагом (? мин).
 * ---
 * int[16][101] ?;       // Номер начального кадра в отрезке?
 * ---
 * int displayMode;      // Режим отображения камер в плеере. (1-4-9 ?)
 * int[16] isExist;      // Есть или нет данные камеры (0-нет, 1-есть).
 * long startDataPos;    // Начало данных в файле.
 * long[15] ?;           // Нули.
 * long endDataPos;      // Конец данных в файле (начало данного блока инфы)
 * long[15] ?;           // Нули.
 * long[16] frameCount;  // Общее кол-во кадров по камерам.
 * <конец файла>
 * -----------------------------------------------------------------------------
 * @author lex
 */
public class Files {

    // Размер инфоблока в конце EXE файла.
    private static final int exeInfoSize =
            4 + 4 + 4 * 16 + 8 * 16 * 101 + 4 * 16 * 101
            + 4 + 16 * 4 + 8 + 15 * 8 + 8 + 15 * 8 + 16 * 8;
    // Список для занесение всех файлов для сканирования.
    private static final ArrayList<File> files = new ArrayList<File>();

    /**
     * Сканирование источника (с рекурсивным обходом подкаталогов).
     * Если источник файл - сканируется один файл, если каталог - сканируются 
     * все файлы в каталоге и во всех подкаталогах. Распознанные файлы 
     * распределяются по камерам и в итоге сортируются по возрастанию по времени 
     * первого кадра.
     * @param startpath Источник (файл или каталог).
     */
    public static void scan(String startpath, int cam) {
        // Сканирование источника.
        String msg = "Построение списка файлов источника...";
        App.log(msg);
        App.mainFrame.setProgressInfo(msg);
        App.mainFrame.startProgress();

        // Установка источника.
        App.srcName = startpath;
        App.srcType = SourceFileFilter.getType(startpath);
        App.srcCamLimit = cam;

        // Отображение источника.
        App.mainFrame.tabSource.displaySource();
        // Очистка отображаемого списка камер источника.
        App.mainFrame.tabSource.displayCams(0);

        // Очистка всех данных о предыдущем сканировании.
        for (int i = 0; i < App.MAXCAMS; i++) {
            App.srcCams[i].clear();
        }
        files.clear();

        // Построение списка файлов для сканирования.
        scanDir(startpath, cam);

        msg = "Сканирование источника...";
        App.log(msg);
        App.mainFrame.setProgressInfo(msg);
        if (files.size() > 0) {
            App.mainFrame.startProgress(1, files.size());

            // Сканирование.
            for (int n = 0; n < files.size(); n++) {
                if (Task.isTerminate()) {
                    break;
                }
                final String msg1 = String.format("Сканирование файла (%d из %d)", n + 1, files.size());
                final String msg2 = files.get(n).getPath();
                App.mainFrame.setProgressInfo(msg1);
                App.mainFrame.setProgressText(msg2);
                App.log(msg1 + ": " + msg2);
                scanFile(files.get(n), cam);
                App.mainFrame.setProgress(n + 1);
            }
            App.mainFrame.setProgress(files.size());

            // Сортировка списков файлов.
            for (int i = 0; i < App.MAXCAMS; i++) {
                Collections.sort(App.srcCams[i].files, FileInfo.getComparator());
            }
        }
        files.clear();

        App.mainFrame.stopProgress();
        msg = "Сканирование источника завершено"
                + (Task.isTerminate() ? " (прервано)." : ".");
        App.mainFrame.setProgressInfo(msg);
        App.log(msg);
        App.mainFrame.tabSource.displayCams();
    }

    /**
     * Построение списка файлов источника с рекурсией вглубь.
     * @param path Источник (файл или каталог).
     */
    private static void scanDir(String path, int cam) {
        try {
            File f = new File(path);
            File[] fa = null;
            // Если источник - каталог, то получаем список его файлов.
            if (f.isDirectory()) {
                fa = f.listFiles(SourceFileFilter.instALL);
            } else {
                // Если источник - файл, добавляем в список только его.
                fa = new File[1];
                fa[0] = f;
            }
            if (fa == null) {
                return;
            }
            if (Task.isTerminate()) {
                return;
            }

            for (int i = 0; i < fa.length; i++) {
                if (fa[i].isDirectory()) { // Каталог.
                    scanDir(fa[i].getPath(), cam); // Переходим глубже на один уровень.
                    continue;
                }
                if (fa[i].length() <= 0) { // Пустой файл.
                    continue;
                }
                if (Task.isTerminate()) {
                    return;
                }
                // Простой файл.
                files.add(fa[i]);
                App.mainFrame.setProgressText(fa[i].getPath());
            }
        } catch (Exception ex) {
            Err.log("File path = " + path);
            Err.log(ex);
        }
    }

    /**
     * Сканирование файла-источника.
     * @param path Источник (файл или каталог).
     */
    private static void scanFile(File file, int cam) {
        try {
            FileType type = SourceFileFilter.getType(file);
            FileInfo info = parseFile(file.getPath(), type, cam);
            if (info != null) {
                // Обрабатываем инфу - добавляем файл ко всем камерам, какие в нём перечислены.
                for (FileInfo.CamData n : info.camInfo) {
                    if (cam == 0 || cam == n.camNumber) {
                        App.srcCams[n.camNumber - 1].addFile(info);
                    }
                }
                if (App.isDebug) {
                    App.log(Type.INFO,
                            (info.frameFirst.pos > 0 ? "Pos=" + info.frameFirst.pos + " " : "")
                            + "file=" + file.getName() + " cams=" + info.camInfo.size()
                            + " [" + info.frameFirst.time.toString()
                            + " - " + info.frameLast.time.toString() + "]");
                }
            }
            Thread.sleep(100);
        } catch (Exception ex) {
            Err.log("File name = " + file);
            Err.log(ex);
        }
    }

    /**
     * Распарсивание файла-источника. Если это EXE - чтение инфы. Распознавание 
     * начального и конечного кадров файла.
     * Используется буферизированный на чтение файл с кешем.
     * @param fileName Имя файла-источника.
     * @param type Тип файла-источника.
     * @param cam Ограничение по камере (0-по всем, иначе только для данной камеры).
     * @return 
     */
    private static FileInfo parseFile(String fileName, FileType type, int cam) {
        try {
            final InputBufferedFile in = new InputBufferedFile(fileName, 100000, 100);
            final int exeplayersize = 1000000; // Минимальный размер встроенного плеера.
            final FileInfo info = new FileInfo();
            info.fileName = fileName;
            info.fileSize = in.getSize();
            info.fileType = type;
            info.startDataPos = 0;
            info.endDataPos = info.fileSize;

            Frame f = new Frame(type);
            final int frameSize = f.getHeaderSize();
            long pos = 0; // Позиция поиска.
            long endpos = in.getSize(); // Конечная позиция.

            // Буфер чтения и парсинга данных.
            final byte[] baFrame = new byte[frameSize];
            final ByteBuffer bbF = ByteBuffer.wrap(baFrame);
            bbF.order(ByteOrder.LITTLE_ENDIAN);

            // Если это EXE - делаем разбор инфы в конце файла.
            if (type == FileType.EXE) {
                long exepos = in.getSize() - exeInfoSize;
                if (exepos < exeplayersize) {
                    return null;
                }
                in.seek(exepos);
                // Инфа по камерам.
                for (int n = 0; n < App.MAXCAMS; n++) {
                    // Наличие данных камер.
                    int isExist = in.readInt(exepos + 8 + (4 * n));
                    if (isExist == -1) {
                        continue; // Камеры нет.
                    }
                    // Если есть такая камера (по которой ограничение), то сбрасываем ограничение
                    // чтобы не отфильтровывало при парсинге файла.
                    if (cam > 0 && cam == n + 1) {
                        cam = 0;
                    }
                    // Смещение первого фрейма.
                    long frameOffs = in.readLong(exepos + 72 + (808 * n));
                    info.addCamData(n + 1, frameOffs, null);
                    if (App.isDebug) {
                        App.log(Type.INFO, "CAM" + (n + 1) + " данные в наличии!");
                    }
                }
                // Позиции начала и конца данных в файле.
                info.startDataPos = in.readLong(exepos + 19532);
                if (info.startDataPos < exeplayersize || info.startDataPos > in.getSize() - exeInfoSize) {
                    return null;
                }
                info.endDataPos = in.readLong(exepos + 19660);
                if (info.endDataPos < exeplayersize || info.endDataPos > in.getSize() - exeInfoSize) {
                    return null;
                }
                pos = info.startDataPos; // Начало.
                endpos = info.endDataPos; // Конец.
            }
            if (App.isDebug) {
                App.log(Type.INFO, "Общий размер данных = " + (endpos - pos));
            }

            // Ищем первый кадр (от начала к концу).
            for (; pos < endpos - frameSize; pos++) {
                in.seek(pos);
                in.read(baFrame, frameSize);
                if (f.parseHeader(bbF, 0) == 0) {
                    break;
                }
            }
            // Разбор не получился или если номер камеры указан и это не он - пропускаем разбор.
            if (!f.isParsed || cam > 0 && f.camNumber != cam) {
                in.close();
                return null;
            }
            f.pos = pos;
            info.frameFirst = f;
            if (info.camInfo.isEmpty()) { // Для HDD инфа заполняется из первого кадра.
                info.addCamData(f.camNumber, f.pos, f.isMain ? f : null);
            }

            // Ищем последний кадр (от конца к началу).
            f = new Frame(type);
            long startpos = pos;
            pos = endpos - frameSize;
            for (; pos >= startpos; pos--) {
                in.seek(pos);
                in.read(baFrame, frameSize);
                if (f.parseHeader(bbF, 0) == 0) {
                    break;
                }
            }
            if (!f.isParsed) { // Разбор не получился.
                in.close();
                return null;
            }
            f.pos = pos;
            info.frameLast = f;

            // Разбор успешный - возвращаем результат.
            //App.log("CAM"+info.camNumber+" file="+info.fileName);
            in.close();
            return info;

        } catch (IOException ioe) {
            Err.log("Source file = " + fileName);
            Err.log(ioe);
            return null;
        }
    }

    /**
     * Получение первого ключевого кадра из файла для заданной камеры.
     * Если данный кадр не распознан - производится поиск и сохранение в инфе,
     * при повторном обращении - возвращает из инфы.
     * @param info Инфа о файле.
     * @param cam Номер камеры.
     * @return Фрейм.
     */
    public static Frame getFirstMainFrame(FileInfo info, int cam) {
        try {
            if (cam < 1 || cam > App.MAXCAMS || info == null) {
                return null;
            }
            FileInfo.CamData ci = info.getCamData(cam);
            // Если такой инфы нет - выход.
            if (ci == null) {
                return null;
            }
            // Если инфа есть и кадр уже распознанн - возвращаем его.
            if (ci.mainFrame != null) {
                return ci.mainFrame;
            }
            // Если кадр не распознанн - распознаём.
            InputBufferedFile in = new InputBufferedFile(info.fileName, 100000, 100);

            Frame f = new Frame(info.fileType);
            int frameSize = f.getHeaderSize();
            long pos = info.startDataPos; // Позиция поиска.
            if (info.fileType == FileType.EXE) {
                pos = ci.mainFrameOffset;
            }
            long endpos = info.endDataPos; // Конечная позиция).

            // Буфер чтения и парсинга данных.
            final byte[] baFrame = new byte[frameSize];
            final ByteBuffer bbF = ByteBuffer.wrap(baFrame);
            bbF.order(ByteOrder.LITTLE_ENDIAN);

            // Ищем первый кадр (от стартовой позиции к конечной).
            for (; pos < endpos - frameSize; pos++) {
                in.seek(pos);
                in.read(baFrame, frameSize);
                if (f.parseHeader(bbF, 0) == 0) {
                    // Если номер камеры указан и это не он - пропускаем.
                    if (f.camNumber == cam && f.isMain) {
                        f.pos = pos;
                        ci.mainFrame = f;
                        return f; // При успехе возвращаем фрейм.
                    }
                    pos += f.getHeaderSize() + f.videoSize + f.audioSize - 1; // -1 т.к. автоинкремент.
                }
            }
            // Разбор не получился.
            in.close();
            return null;

        } catch (IOException ioe) {
            Err.log("File info = " + info.toString());
            Err.log(ioe);
            return null;
        }
    }

    /**
     * Возвращает имя файла/пути без расширения.
     * @param name Имя файла.
     * @return Имя файла без расширения.
     */
    public static String getNameWOExt(String name) {
        // Пропускаем последний разделитель.
        int n = name.lastIndexOf(File.separatorChar);
        // Ищем последнюю точку.
        int nd = name.lastIndexOf('.');
        if (nd > -1 && nd > n) {
            // Если точка есть и она до разделителя - режем до точки.
            name = name.substring(0, nd);
        }
        return name;
    }
}
