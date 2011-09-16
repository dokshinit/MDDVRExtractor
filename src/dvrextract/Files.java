package dvrextract;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collections;

/**
 * TODO: Добавить в парсинг EXE - распарсивание конца файла на начальные отступы для первых фреймов камер!
 * Осуществление действий с источником: сканирование, обработка.
 * @author lex
 */
public class Files {

    /**
     * Сканирование источника (с рекурсивным обходом подкаталогов).
     * Если источник файл - сканируется один файл, если каталог - сканируются 
     * все файлы в каталоге и во всех подкаталогах. Распознанные файлы 
     * распределяются по камерам и в итоге сортируются по возрастанию по времени 
     * первого кадра.
     * @param startpath Источник (файл или каталог).
     */
    public void scan(String startpath, int cam) {
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
    }

    /**
     * Сканирование уровня источника с рекурсией вглубь.
     * @param path Источник (файл или каталог).
     */
    private void scanDir(String path, int cam) {
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

            for (int i = 0; i < fa.length; i++) {
                if (fa[i].isDirectory()) { // Каталог.
                    scanDir(fa[i].getPath(), cam); // Переходим глубже на один уровень.
                    continue;
                }
                if (fa[i].length() <= 0) { // Пустой файл.
                    continue;
                }
                // Простой файл.
                FileType type = SourceFileFilter.getType(fa[i]);
                FileInfo info = parseFileBuffered(fa[i].getPath(), type, cam);
                if (info != null) {
                    // Добавляем файл ко всем камерам, какие в нём перечислены.
                    for (int n : info.camNumbers) {
                        App.srcCams[n - 1].addFile(info);
                        App.log((info.frameFirst.pos > 0
                                ? "Pos=" + info.frameFirst.pos + " " : "")
                                + "file=" + fa[i].getPath() + " cam=" + n
                                + " t=" + (info.frameLast.time.getTime()
                                - info.frameFirst.time.getTime())
                                + " time1=" + info.frameFirst.time.toString()
                                + " time2=" + info.frameLast.time.toString());
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Распознавание начального и конечного кадров файла.
     * Создание записи информации о файле и добавление её в массив соответсвенной камере.
     * @param fileName Имя файла.
     */
    // <editor-fold defaultstate="collapsed" desc="Deprecated parseFile(...)">
    @Deprecated
    public FileInfo parseHDDFile(String fileName, int cam) {
        try {
            final byte[] baFrame = new byte[100]; // один фрейм
            final ByteBuffer bbF = ByteBuffer.wrap(baFrame);
            bbF.order(ByteOrder.LITTLE_ENDIAN);

            InputData in = new InputData(fileName);

            FileInfo info = new FileInfo();
            info.fileName = fileName;
            info.fileSize = in.getSize();
            info.frameFirst = new Frame(FileType.HDD);
            info.frameLast = new Frame(FileType.HDD);
            Frame f = new Frame(FileType.HDD);

            // Ищем первый кадр.
            long pos = 0;
            while (pos < in.getSize() - f.getHeaderSize()) {
                in.seek(pos);
                in.read(baFrame, f.getHeaderSize());
                if (info.frameFirst.parseHeader(bbF, 0) == 0) {
                    // Если номер камеры указан и это не он - пропускаем разбор.
                    if (cam > 0 && info.frameFirst.camNumber != cam) {
                        in.close();
                        return null;
                    }
                    info.frameFirst.pos = pos;
                    info.camNumbers.add(info.frameFirst.camNumber); // из первого кадра!
                    break;
                }
                pos++;
            }
            if (info.frameFirst.pos == -1) {
                // разбор не получился.
                in.close();
                return null;
            }
            // Ищем последний кадр.
            pos = in.getSize() - f.getHeaderSize();
            while (pos >= 0) {
                in.seek(pos);
                in.read(baFrame, f.getHeaderSize());
                if (info.frameLast.parseHeader(bbF, 0) == 0) {
                    info.frameLast.pos = pos;
                    break;
                }
                pos--;
            }
            if (info.frameLast.pos == -1) {
                // разбор не получился.
                in.close();
                return null;
            }
            in.close();
            return info;

        } catch (IOException ioe) {
            ioe.printStackTrace();
            return null;
        }
    }
    // </editor-fold>

    /**
     * Распознавание начального и конечного кадров файла.
     * Создание записи информации о файле и добавление её в массив соответсвенной камере.
     * @param fileName Имя файла.
     */
    // <editor-fold defaultstate="collapsed" desc="Deprecated parseHDDFile(...)">
    @Deprecated
    public FileInfo parseHDDFileBuffered(String fileName, int cam) {
        try {
            final byte[] baFrame = new byte[100000]; // буфер чтения
            final ByteBuffer bbF = ByteBuffer.wrap(baFrame);
            bbF.order(ByteOrder.LITTLE_ENDIAN);

            InputData in = new InputData(fileName);

            FileInfo info = new FileInfo();
            info.fileName = fileName;
            info.fileSize = in.getSize();
            Frame f = new Frame(FileType.HDD);

            // Ищем первый кадр (от начала к концу).
            long pos = 0;
            long ost = in.getSize();
            while (pos < in.getSize() - f.getHeaderSize()) {
                in.seek(pos);
                int len = (int) Math.min(baFrame.length, ost);
                in.read(baFrame, (int) len);

                for (int i = 0; i < len - f.getHeaderSize(); i++) {
                    if (f.parseHeader(bbF, i) == 0) {
                        // Если номер камеры указан и это не он - пропускаем разбор.
                        if (cam > 0 && f.camNumber != cam) {
                            in.close();
                            return null;
                        }
                        f.pos = pos + i;
                        info.camNumbers.add(f.camNumber); // Из первого кадра!
                        info.frameFirst = f;
                        break;
                    }
                }
                if (f.isParsed) {
                    break;
                } else {
                    pos += len - f.getHeaderSize();
                    ost -= len - f.getHeaderSize();
                }
            }
            if (!f.isParsed) {
                // Разбор не получился.
                in.close();
                return null;
            }

            // Ищем последний кадр (от конца к началу).
            f = new Frame(FileType.HDD);
            pos = in.getSize();
            ost = in.getSize();
            while (pos > 0) {
                int len = (int) Math.min(baFrame.length, ost);
                pos -= len;
                in.seek(pos);
                in.read(baFrame, (int) len);
                for (int i = len - f.getHeaderSize(); i >= 0; i--) {
                    if (f.parseHeader(bbF, i) == 0) {
                        f.pos = pos + i;
                        info.frameLast = f;
                        break;
                    }
                }
                if (f.isParsed) {
                    break;
                } else {
                    ost -= len - f.getHeaderSize();
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
    // </editor-fold>

    /**
     * Сканирование файла-источника. Если это EXE - чтение инфы. Распознавание 
     * начального и конечного кадров файла.
     * @param fileName Имя файла-источника.
     * @param type Тип файла-источника.
     * @param cam Ограничение по камере (0-по всем, иначе только для данной камеры).
     * @return 
     */
    public FileInfo parseFileBuffered(String fileName, FileType type, int cam) {
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

            Frame f = new Frame(type);
            long pos = 0; // Позиция поиска.
            long ost = in.getSize(); // Остаток данных.
            long size = in.getSize(); // Размер данных (конечная позиция).
            int frameSize = f.getHeaderSize();

            // Если это EXE - делаем разбор инфы в конце файла.
            if (type == FileType.EXE) {
                in.seek(in.getSize() - 28 * 16);
                in.read(baFrame, 28 * 16);

                // Наличие треков камер.
                for (int i = 0; i < App.MAXCAMS; i++) {
                    if (bbF.getInt() != 0 && (cam == 0 || cam == (i + 1))) {
                        info.camNumbers.add(i + 1);
                        App.log("CAM" + (i + 1) + " данные в наличии!");
                    }
                }
                // Позиции начала и конца данных в файле.
                pos = bbF.getInt(64);
                size = bbF.getInt(12 * 16);
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
                        if (info.camNumbers.isEmpty()) {
                            info.camNumbers.add(f.camNumber); // Из первого кадра!
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
}
