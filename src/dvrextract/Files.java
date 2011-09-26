package dvrextract;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collections;

/**
 * Осуществление действий с источником: сканирование, обработка.
 * @author lex
 */
public class Files {

    // Размер инфоблока в конце EXE файла.
    private static final int exeInfoSize =
            4 + 4 + 4 * 16 + 8 * 16 * 101 + 4 * 16 * 101
            + 4 + 16 * 4 + 8 + 15 * 8 + 8 + 15 * 8 + 16 * 8;

    /**
     * Сканирование источника (с рекурсивным обходом подкаталогов).
     * Если источник файл - сканируется один файл, если каталог - сканируются 
     * все файлы в каталоге и во всех подкаталогах. Распознанные файлы 
     * распределяются по камерам и в итоге сортируются по возрастанию по времени 
     * первого кадра.
     * @param startpath Источник (файл или каталог).
     */
    public static void scan(String startpath, int cam) {
        App.mainFrame.setProgressInfo("Сканирование источника.");
        App.mainFrame.startProgress();
        // Очистка всех данных о предыдущем сканировании.
        for (int i = 0; i < App.MAXCAMS; i++) {
            App.srcCams[i].clear();
        }
        // Сканирование.
        scanDir(startpath, cam);
        // Сортировка списков файлов.
        for (int i = 0; i < App.MAXCAMS; i++) {
            Collections.sort(App.srcCams[i].files, FileInfo.getComparator());
        }
        App.mainFrame.stopProgress();
        App.mainFrame.setProgressInfo("Сканирование источника завершено"
                + (App.isTaskCancel() ? " (прервано)." : "."));
    }

    /**
     * Сканирование уровня источника с рекурсией вглубь.
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
            if (App.isTaskCancel()) {
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
                // Простой файл.
                if (App.isTaskCancel()) {
                    return;
                }
                App.mainFrame.setProgressInfo("Сканирование источника: " + fa[i].getName());
                Thread.sleep(100);
                FileType type = SourceFileFilter.getType(fa[i]);
                FileInfo info = parseFileBuffered(fa[i].getPath(), type, cam);
                if (info != null) {
                    // Обрабатываем инфу - добавляем файл ко всем камерам, какие в нём перечислены.
                    for (FileInfo.CamData n : info.camInfo) {
                        App.srcCams[n.camNumber - 1].addFile(info);
                    }
                    App.log((info.frameFirst.pos > 0
                            ? "Pos=" + info.frameFirst.pos + " " : "")
                            + "file=" + fa[i].getPath() + " cams=" + info.camInfo.size()
                            + " [" + info.frameFirst.time.toString()
                            + " - " + info.frameLast.time.toString() + "]");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Сканирование файла-источника. Если это EXE - чтение инфы. Распознавание 
     * начального и конечного кадров файла.
     * @param fileName Имя файла-источника.
     * @param type Тип файла-источника.
     * @param cam Ограничение по камере (0-по всем, иначе только для данной камеры).
     * @return 
     */
    private static FileInfo parseFileBuffered(String fileName, FileType type, int cam) {
        try {
            // Буфер чтения и парсинга данных.
            final byte[] baFrame = new byte[100000];
            final ByteBuffer bbF = ByteBuffer.wrap(baFrame);
            bbF.order(ByteOrder.LITTLE_ENDIAN);

            InputData in = new InputData(fileName);

            FileInfo info = new FileInfo();
            info.fileName = fileName;
            info.fileSize = in.getSize();
            info.fileType = type;
            info.startDataPos = 0;
            info.endDataPos = info.fileSize;

            Frame f = new Frame(type);
            long pos = 0; // Позиция поиска.
            long ost = in.getSize(); // Остаток данных.
            long size = in.getSize(); // Размер данных (конечная позиция).
            int frameSize = f.getHeaderSize();

            // Если это EXE - делаем разбор инфы в конце файла.
            if (type == FileType.EXE) {
                if (in.getSize() < exeInfoSize + 1024000) {
                    return null;
                }
                in.seek(in.getSize() - exeInfoSize);
                in.read(baFrame, exeInfoSize);
                // Инфа по камерам.
                for (int n = 0; n < App.MAXCAMS; n++) {
                    // Наличие данных камер.
                    int isExist = bbF.getInt(8 + (4 * n));
                    if (isExist == -1) {
                        continue; // Камеры нет.
                    }
                    // Смещение первого фрейма.
                    long frameOffs = bbF.getLong(72 + (808 * n));
                    info.addCamData(n + 1, frameOffs, null);
                    App.log("CAM" + (n + 1) + " данные в наличии!");
                }
                // Позиции начала и конца данных в файле.
                info.startDataPos = bbF.getLong(19532);
                if (info.startDataPos < 1024000 || info.startDataPos > in.getSize() - exeInfoSize) {
                    return null;
                }
                info.endDataPos = bbF.getLong(19660);
                if (info.endDataPos < 1024000 || info.endDataPos > in.getSize() - exeInfoSize) {
                    return null;
                }
                pos = info.startDataPos; // Начало.
                size = info.endDataPos; // Конец.
                ost = size - pos;
            }
            App.log("Общий размер данных = " + ost);

            // Ищем первый кадр (от начала к концу).
            while (pos < size - frameSize) {
                in.seek(pos);
                int len = (int) Math.min(baFrame.length, ost);
                in.read(baFrame, (int) len);

                for (int i = 0; i < len - frameSize; i++) {
                    if (f.parseHeader(bbF, i) == 0) {
                        // Если номер камеры указан и это не он - пропускаем разбор.
                        if (cam > 0 && f.camNumber != cam) {
                            in.close();
                            return null;
                        }
                        f.pos = pos + i;
                        if (info.camInfo.isEmpty()) {
                            // Для HDD инфа заполняется из первого кадра.
                            info.addCamData(f.camNumber, f.pos, f.isMain ? f : null);
                        }
                        info.frameFirst = f;
                        break;
                    }
                }
                if (f.isParsed) {
                    break;
                } else {
                    pos += len - frameSize;
                    ost -= len - frameSize;
                }
            }
            if (!f.isParsed) {
                // Разбор не получился.
                in.close();
                return null;
            }

            // Ищем последний кадр (от конца к началу).
            f = new Frame(type);
            pos = size;
            ost = size;
            while (pos > 0) {
                int len = (int) Math.min(baFrame.length, ost);
                pos -= len;
                in.seek(pos);
                in.read(baFrame, (int) len);
                for (int i = len - frameSize; i >= 0; i--) {
                    if (f.parseHeader(bbF, i) == 0) {
                        f.pos = pos + i;
                        info.frameLast = f;
                        break;
                    }
                }
                if (f.isParsed) {
                    break;
                } else {
                    ost -= len - frameSize;
                }
            }
            if (!f.isParsed) {
                // Разбор не получился.
                in.close();
                return null;
            }

            // Разбор успешный - возвращаем результат.
            //App.log("CAM"+info.camNumber+" file="+info.fileName);
            in.close();
            return info;

        } catch (IOException ioe) {
            ioe.printStackTrace();
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
            // Буфер чтения и парсинга данных.
            final byte[] baFrame = new byte[100000];
            final ByteBuffer bbF = ByteBuffer.wrap(baFrame);
            bbF.order(ByteOrder.LITTLE_ENDIAN);

            InputData in = new InputData(info.fileName);
            Frame f = new Frame(info.fileType);

            long pos = info.startDataPos; // Позиция поиска.
            long size = info.endDataPos; // Размер данных (конечная позиция).
            long ost = size - pos; // Остаток данных.
            int frameSize = f.getHeaderSize();

            if (info.fileType == FileType.EXE) {
                pos = ci.mainFrameOffset;
                ost = size - pos;
            }

            // Ищем первый кадр (от стартовой позиции к конечной).
            while (pos < size - frameSize) {
                in.seek(pos);
                int len = (int) Math.min(baFrame.length, ost);
                in.read(baFrame, (int) len);
                int i = 0;
                for (; i < len - frameSize; i++) {
                    if (f.parseHeader(bbF, i) == 0) {
                        // Если номер камеры указан и это не он - пропускаем разбор.
                        if (f.camNumber == cam && f.isMain) {
                            f.pos = pos + i;
                            ci.mainFrame = f;
                            return f; // При успехе возвращаем фрейм.
                        }
                        i += f.getHeaderSize() + f.videoSize + f.audioSize - 1; // -1 т.к. автоинкремент.
                    }
                }
                pos += i;
                ost -= i;
            }
            // Разбор не получился.
            in.close();
            return null;

        } catch (IOException ioe) {
            ioe.printStackTrace();
            return null;
        }
    }
}

/*
 * ФОРМАТ ОПИСАТЕЛЯ EXE ФАЙЛА:
 
 * <конец данных>
 * int ?; =2
 * int ?; =1
 * int[16] ?; // (=0 - если камера есть, =-1 - если нет)
 * ---
 * long[16][101] offset; // Смещение в файле до опорных кадров с шагом (? мин).
 * ---
 * int[16][101] ?; // Номер начального кадра в отрезке?
 * ---
 * int displayMode; // Режим отображения камер в плеере. (1-4-9 ?)
 * int[16] isExist; // Есть или нет данные камеры (0-нет, 1-есть).
 * long startDataPos; // Начало данных в файле.
 * long[15] ?; // Нули.
 * long endDataPos; // Конец данных в файле (начало данного блока инфы)
 * long[15] ?; // Нули.
 * long[16] frameCount; // Общее кол-во кадров по камерам.
 * <конец файла>
 */