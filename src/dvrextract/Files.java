/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dvrextract;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Pattern;

/**
 *
 * @author lex
 */
public class Files {

    /**
     * Рекурсивное построение списков файлов по фильтру (с обходом подкаталогов).
     * Списки файлов формируются для каждой камеры отдельно. Далее списки 
     * сортируются по возрастанию по времени первого кадра.
     * @param startpath Путь к каталогу сканирования.
     */
    public void scan(String startpath, int cam) {
        for (int i = 0; i < App.MAXCAMS; i++) {
            App.srcCams[i].clear();
        }
        scanDir(startpath, cam);
        for (int i = 0; i < App.MAXCAMS; i++) {
            Collections.sort(App.srcCams[i].files, FileInfo.getComparator());
        }
    }

    /**
     * Рекурсивное построение списка файлов по фильтру (с обходом подкаталогов).
     * @param path Путь к каталогу сканирования.
     */
    private void scanDir(String path, int cam) {
        try {
            File f = new File(path);
            File[] fa = f.listFiles(new FileFilter() {

                @Override
                public boolean accept(File pathname) {
                    if (pathname.isDirectory()) {
                        return true;
                    }
                    if (pathname.length() == 0) {
                        return false;
                    }
                    return SourceFileFilter.instEXE.accept(pathname)
                            || SourceFileFilter.instHDD.accept(pathname);
                }
            });

            for (int i = 0; i < fa.length; i++) {
                if (fa[i].isDirectory()) {
                    scanDir(fa[i].getPath(), cam); // Переходим глубже на один уровень.
                } else {
                    FileInfo info = null;
                    if (SourceFileFilter.instEXE.accept(fa[i])) {
                        info = parseEXEFileBuffered(fa[i].getPath(), 1, cam);
                    } else if (SourceFileFilter.instHDD.accept(fa[i])) {
                        info = parseHDDFileBuffered(fa[i].getPath(), cam);
                        //App.log("file=" + fa[i].getPath());
                    }
                    if (info != null) {
                        // Добавляем файл ко всем камерам, какие в нём перечислены.
                        for (int n : info.camNumbers) {
                            App.srcCams[n - 1].addFile(info);
                            App.log((info.frameFirst.pos > 0 ? "ERR=" + info.frameFirst.pos + " " : "")
                                    + "file=" + fa[i].getPath() + " cam=" + n
                                    + " t=" + (info.frameLast.time.getTime() - info.frameFirst.time.getTime())
                                    + " time1=" + info.frameFirst.time.toString() + " time2=" + info.frameLast.time.toString());
                        }
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
            info.frameFirst = new Frame();
            info.frameLast = new Frame();
            Frame f = new Frame();

            // Ищем первый кадр.
            long pos = 0;
            while (pos < in.getSize() - Frame.HDD_HSIZE) {
                in.seek(pos);
                in.read(baFrame, Frame.HDD_HSIZE);
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
            pos = in.getSize() - Frame.HDD_HSIZE;
            while (pos >= 0) {
                in.seek(pos);
                in.read(baFrame, Frame.HDD_HSIZE);
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

    /**
     * Распознавание начального и конечного кадров файла.
     * Создание записи информации о файле и добавление её в массив соответсвенной камере.
     * @param fileName Имя файла.
     */
    public FileInfo parseHDDFileBuffered(String fileName, int cam) {
        try {
            final byte[] baFrame = new byte[100000]; // буфер чтения
            final ByteBuffer bbF = ByteBuffer.wrap(baFrame);
            bbF.order(ByteOrder.LITTLE_ENDIAN);

            InputData in = new InputData(fileName);

            FileInfo info = new FileInfo();
            info.fileName = fileName;
            info.fileSize = in.getSize();
            Frame f = new Frame();

            // Ищем первый кадр (от начала к концу).
            long pos = 0;
            long ost = in.getSize();
            while (pos < in.getSize() - Frame.HDD_HSIZE) {
                in.seek(pos);
                int len = (int) Math.min(baFrame.length, ost);
                in.read(baFrame, (int) len);

                for (int i = 0; i < len - Frame.HDD_HSIZE; i++) {
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
                    pos += len - Frame.HDD_HSIZE;
                    ost -= len - Frame.HDD_HSIZE;
                }
            }
            if (!f.isParsed) {
                // Разбор не получился.
                in.close();
                return null;
            }

            // Ищем последний кадр (от конца к началу).
            f = new Frame();
            pos = in.getSize();
            ost = in.getSize();
            while (pos > 0) {
                int len = (int) Math.min(baFrame.length, ost);
                pos -= len;
                in.seek(pos);
                in.read(baFrame, (int) len);
                for (int i = len - Frame.HDD_HSIZE; i >= 0; i--) {
                    if (f.parseHeader(bbF, i) == 0) {
                        f.pos = pos + i;
                        info.frameLast = f;
                        break;
                    }
                }
                if (f.isParsed) {
                    break;
                } else {
                    ost -= len - Frame.HDD_HSIZE;
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
     * Распознавание начального и конечного кадров файла.
     * Создание записи информации о файле и добавление её в массив соответсвенной камере.
     * @param fileName Имя файла.
     */
    public FileInfo parseEXEFileBuffered(String fileName, int type, int cam) {
        try {
            final byte[] baFrame = new byte[100000]; // буфер чтения
            final ByteBuffer bbF = ByteBuffer.wrap(baFrame);
            bbF.order(ByteOrder.LITTLE_ENDIAN);

            InputData in = new InputData(fileName);

            FileInfo info = new FileInfo();
            info.fileName = fileName;
            info.fileSize = in.getSize();
            Frame f = new Frame();

            long pos = 0;
            long ost = in.getSize();
            long size = in.getSize();

            // Если это EXE - делаем разбор инфы в конце файла.
            if (type == 1) {
                in.seek(in.getSize() - 28 * 16);
                in.read(baFrame, 28 * 16);

                // Наличие треков камер.
                for (int i = 0; i < App.MAXCAMS; i++) {
                    if (bbF.getInt() != 0 && (cam == 0 || cam == (i+1))) {
                        info.camNumbers.add(i);
                        App.log("CAM" + (i + 1) + " данные в наличии!");
                    }
                }
                // Позиции начала и конца данных в файле.
                pos = bbF.getInt(24*16);
                size = bbF.getInt(12 * 16);
                ost = size - pos;
                App.log("Общий размер данных = " + ost);
            }

            // Ищем первый кадр (от начала к концу).
            while (pos < in.getSize() - Frame.HDD_HSIZE) {
                in.seek(pos);
                int len = (int) Math.min(baFrame.length, ost);
                in.read(baFrame, (int) len);

                for (int i = 0; i < len - Frame.HDD_HSIZE; i++) {
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
                    pos += len - Frame.HDD_HSIZE;
                    ost -= len - Frame.HDD_HSIZE;
                }
            }
            if (!f.isParsed) {
                // Разбор не получился.
                in.close();
                return null;
            }

            // Ищем последний кадр (от конца к началу).
            f = new Frame();
            pos = size;
            ost = size;
            while (pos > 0) {
                int len = (int) Math.min(baFrame.length, ost);
                pos -= len;
                in.seek(pos);
                in.read(baFrame, (int) len);
                for (int i = len - Frame.HDD_HSIZE; i >= 0; i--) {
                    if (f.parseHeader(bbF, i) == 0) {
                        f.pos = pos + i;
                        info.frameLast = f;
                        break;
                    }
                }
                if (f.isParsed) {
                    break;
                } else {
                    ost -= len - Frame.HDD_HSIZE;
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