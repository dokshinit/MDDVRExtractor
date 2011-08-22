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
public class HDDFiles {

    // Фильтр для отбора обрабатываемых файлов: daNNNNN - где N - номер файла.
    private static final Pattern ptrn = Pattern.compile("da.+",
            Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);
    // Сканируемый каталог.
    private String name;
    // Массив "камер" с массивами информации о файлах камеры.
    public final ArrayList<HDDFileInfo>[] files = new ArrayList[App.MAXCAMS];

    /**
     * 
     * @param pathName 
     */
    public HDDFiles(String pathName) {
        name = pathName;
        for (int i = 0; i < files.length; i++) {
            files[i] = new ArrayList<HDDFileInfo>();
        }
    }

    /**
     * Рекурсивное построение списков файлов по фильтру (с обходом подкаталогов).
     * Списки файлов формируются для каждой камеры отдельно. Далее списки 
     * сортируются по возрастанию по времени первого кадра.
     * @param path Путь к каталогу сканирования.
     */
    public void scan(int cam) {
        scanDir(cam, name);
        for (int i = 0; i < files.length; i++) {
            Collections.sort(files[i], HDDFileInfo.getComparator());
        }
    }

    /**
     * Рекурсивное построение списка файлов по фильтру (с обходом подкаталогов).
     * @param path Путь к каталогу сканирования.
     */
    private void scanDir(int cam, String path) {
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
                    return ptrn.matcher(pathname.getName()).matches();
                }
            });

            for (int i = 0; i < fa.length; i++) {
                if (fa[i].isDirectory()) {
                    scanDir(cam, fa[i].getPath()); // Переходим глубже на один уровень.
                } else {
                    HDDFileInfo info = parseFileBuffered(cam, fa[i].getPath());
                    if (info != null) {
                        files[info.camNumber - 1].add(info);
                        App.log((info.frameFirst.pos > 0 ? "ERR=" + info.frameFirst.pos + " " : "")
                                + "file=" + fa[i].getPath() + " cam=" + info.camNumber
                                + " t=" + (info.frameLast.time.getTime() - info.frameFirst.time.getTime())
                                + " time1=" + info.frameFirst.time.toString() + " time2=" + info.frameLast.time.toString());
                    } else {
                        //App.log("file=" + fa[i].getPath());
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
    public HDDFileInfo parseFile(int cam, String fileName) {
        try {
            final byte[] baFrame = new byte[100]; // один фрейм
            final ByteBuffer bbF = ByteBuffer.wrap(baFrame);
            bbF.order(ByteOrder.LITTLE_ENDIAN);

            InputData in = new InputData(fileName);

            HDDFileInfo info = new HDDFileInfo();
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
                    info.camNumber = info.frameFirst.camNumber; // из первого кадра!
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
    public HDDFileInfo parseFileBuffered(int cam, String fileName) {
        try {
            final byte[] baFrame = new byte[100000]; // буфер чтения
            final ByteBuffer bbF = ByteBuffer.wrap(baFrame);
            bbF.order(ByteOrder.LITTLE_ENDIAN);

            InputData in = new InputData(fileName);

            HDDFileInfo info = new HDDFileInfo();
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
                        info.camNumber = f.camNumber; // Из первого кадра!
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
}
