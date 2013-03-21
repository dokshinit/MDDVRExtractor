/*
 * Copyright (c) 2011-2012, Aleksey Nikolaevich Dokshin. All right reserved.
 * Contacts: dant.it@gmail.com, dokshin@list.ru.
 */
package mddvrextract.xfsengine;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Pattern;

/**
 * Обеспечение низкоуровневых операций чтения HDD.
 *
 * <pre>
 * Windows: Пользователю необходимо иметь права Администратора.
 *   Пример: \\.\PhysicalDrive0
 * Linux: Пользователю необходимо быть членом группы disk или дать доступ к
 * устройству на чтение.
 *   Пример: /dev/sdb
 * </pre>
 *
 * @author Докшин Алексей Николаевич <dant.it@gmail.com>
 */
public class Device {

    /**
     * Файл устройства для низкоуровневого доступа.
     */
    private final RandomAccessFile fio;
    /**
     * Флаг состояния устройства: true - открыто и можно читать, false -
     * закрыто.
     */
    private boolean isOpen;
    /**
     * Буфер для чтения (для случая выравнивания чтения по размеру блока
     * устройства).
     */
    private byte[] buffer;
    /**
     * Размер блока устройства (для случая выравнивания чтения по размеру блока
     * устройства).
     */
    private int bsize;
    /**
     * Логарифм 2 по размеру блока (для случая выравнивания чтения по размеру
     * блока устройства).
     */
    private int log2_bsize;

    /**
     * Конструктор. Одновременно происходит и открытие устройства.
     *
     * @param name Имя файла устройства. См. в описании класса.
     * @throws FileNotFoundException Если файл не найден.
     * @throws IOException Ошибка ввода-вывода.
     */
    public Device(String name) throws FileNotFoundException, IOException {
        isOpen = false;
        buffer = null;
        bsize = 0;

        fio = new RandomAccessFile(name, "r");
        byte[] b = new byte[65536];
        // Проверка на произвольный доступ.
        fio.seek(0);
        try {
            fio.read(b, 0, 1);
            isOpen = true;

        } catch (Exception ex) {
            // Поиск размера сектора.
            for (int n = 8; n <= 16; n++) {
                fio.seek(0);
                try {
                    fio.read(b, 0, 1 << n);
                    // Нашли!
                    log2_bsize = n;
                    bsize = 1 << n;
                    buffer = new byte[bsize * 2];
                    return;
                } catch (Exception ex2) {
                }
            }
            throw new IOException("Sector size not found!");
        }
    }

    /**
     * Возвращает список названий устройств HDD в системе.
     *
     * @return Список названий устройств.
     */
    public static ArrayList<String> list() {
        int os = 0;
        final int LINUX = 1;
        final int WINDOWS = 2;
        // Определяем тип ОС.
        String s = System.getProperty("os.name").toLowerCase();
        if (s.substring(0, 3).equals("lin")) {
            os = LINUX;
        } else if (s.substring(0, 3).equals("win")) {
            os = WINDOWS;
        }
        ArrayList<String> a = new ArrayList<String>();

        switch (os) {
            case LINUX:
                final Pattern p = Pattern.compile("^[hs]d[a-z]$", Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);
                final File dir = new File("/dev");
                final String[] ss = dir.list();
                for (int i = 0; i < ss.length; i++) {
                    if (p.matcher(ss[i]).matches()) {
                        a.add("/dev/" + ss[i]);
                    }
                }
                break;
            case WINDOWS:
                final String prefix = "\\\\.\\PhysicalDrive";
                for (int i = 0; i < 10; i++) {
                    /*
                     * Проверка попыткой открытия (проверка с помощью File на
                     * существование и на возможность чтения всегда возвращает
                     * false).
                     */
                    try {
                        final FileInputStream fis = new FileInputStream(prefix + i);
                        a.add(prefix + i);
                        fis.close();
                    } catch (Exception ex) {
                    }
                }
                break;
        }
        Collections.sort(a);
        return a;
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    /**
     * Закрытие устройства.
     */
    public void close() {
        synchronized (fio) {
            try {
                fio.close();
            } catch (IOException ex) {
            }
            isOpen = false;
        }
    }

    /**
     * Чтение из устройства (гарантированное прочтение при наличии данных, при
     * выходе за пределы устройства - ошибка!!!).
     *
     * @param buf Буфер для данных.
     * @param index Начальная позиция в буфере.
     * @param pos Начальная позиция в файле.
     * @param size Размер данных в байтах.
     * @return Количество считанных байт.
     * @throws IOException Ошибка выполнения операции.
     */
    public int read(byte[] buf, int index, long pos, int size) throws IOException {
        int n = 0;
        if (buffer == null) {
            // Обычное чтение.
            fio.seek(pos);
            for (int step = 1; size > 0 && step != 0; index += step, size -= step, n += step) {
                step = fio.read(buf, index, size);
                if (step == -1) {
                    throw new IOException("Incomplete read end of device!");
                }
            }
        } else {
            // Буферизованное чтение из-за необходимости выранивания по сектору.
            long opos = (pos >> log2_bsize) << log2_bsize;
            fio.seek(opos);
            int offs = (int) (pos - opos);
            if (offs > 0) {
                // Первый неполный блок.
                int sz = fio.read(buffer, 0, bsize);
                if (sz != bsize) { // Нельзя считать меньше, чем один блок!
                    throw new IOException("Incomplete read first block!");
                }
                for (; offs < bsize && size > 0; offs++, index++, size--, n++) {
                    buf[index] = buffer[offs];
                }
            }
            int osize = (size >> log2_bsize) << log2_bsize;
            if (osize > 0) {
                // Средняя часть кратная блокам.
                size -= osize;
                for (int step = 1; osize > 0 && step != 0; index += step, osize -= step, n += step) {
                    step = fio.read(buf, index, osize);
                    if (step == -1) {
                        return n; // Прерываем чтение.
                    }
                }
                if (osize != 0) {
                    throw new IOException("Incomplete read middle block!");
                }
            }
            if (size > 0) {
                // Последний неполный блок.
                int sz = fio.read(buffer, 0, bsize);
                if (sz != bsize) { // Нельзя считать меньше, чем один блок!
                    throw new IOException("Incomplete read last block!");
                }
                for (int i = 0; size > 0; i++, index++, size--, n++) {
                    buf[index] = buffer[i];
                }
            }
        }
        return n;
    }

    /**
     * Чтение из устройства (в начало буфера).
     *
     * @param buffer Буфер для данных.
     * @param pos Начальная позиция в файле.
     * @param size Размер данных в байтах.
     * @return Количество считанных байт.
     * @throws IOException Ошибка ввода/вывода.
     */
    public int read(byte[] buffer, long pos, int size) throws IOException {
        return read(buffer, 0, pos, size);
    }
}
