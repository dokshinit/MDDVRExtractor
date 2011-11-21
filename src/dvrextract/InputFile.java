/*
 * Copyright (c) 2011, Aleksey Nikolaevich Dokshin. All right reserved.
 * Contacts: dant.it@gmail.com, dokshin@list.ru.
 */
package dvrextract;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Низкоуровневые операции с файлом-источником.
 * @author Докшин Алексей Николаевич <dant.it@gmail.com>
 */
public class InputFile {

    /**
     * Имя файла.
     */
    private String name;
    /**
     * Файл с произвольным доступом (из-за EXE и сканирования).
     */
    private RandomAccessFile in;

    /**
     * Конструктор.
     * @param fileName Имя файла-источника.
     * @throws FileNotFoundException Ошибка при отсутствии файла.
     * @throws IOException Ошибка при позиционировании.
     */
    public InputFile(String fileName) throws FileNotFoundException, IOException {
        name = fileName;
        in = null;
        if (name != null) {
            in = new RandomAccessFile(name, "r");
            in.seek(0);
        }
    }

    /**
     * Возвращает размер файла.
     * @return Размер файла.
     * @throws IOException Ошибка ввода-вывода.
     */
    public long getSize() throws IOException {
        return in.length();
    }

    /**
     * Позиционирует текущий указатель чтения/записи на заданную позицию от
     * начала файла.
     * @param n Позиция.
     * @throws IOException Ошибка ввода-вывода.
     */
    public void seek(long n) throws IOException {
        in.seek(n);
    }

    /**
     * Пропуск указанного кол-ва байт от текущей позиции (смещение позиции на
     * указанное кол-во байт).
     * @param n Смещение в байтах.
     * @throws IOException Ошибка ввода-вывода.
     */
    public void skip(int n) throws IOException {
        while (n > 0) {
            n -= in.skipBytes(n);
        }
    }

    /**
     * Чтение блока данных с текущей позиции в буфер (с начала).
     * @param ba Буфер.
     * @param size Размер данных в байтах.
     * @throws IOException Ошибка ввода-вывода.
     */
    public void read(byte[] ba, int size) throws IOException {
        int readed = 1, pos = 0;
        while (readed >= 0 && pos < size) {
            readed = in.read(ba, pos, size - pos);
            pos += readed;
        }
    }

    /**
     * Закрытие файла-источника.
     * @throws IOException Ошибка ввода-вывода.
     */
    public void close() throws IOException {
        if (in != null) {
            in.close();
        }
    }

    /**
     * Безопасный вариант закрытия файла-источника (не вызывает исключений).
     */
    public void closeSafe() {
        try {
            close();
        } catch (IOException ex) {
        }
    }
}
