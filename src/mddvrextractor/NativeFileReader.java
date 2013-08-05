/*
 * Copyright (c) 2011-2013, Aleksey Nikolaevich Dokshin. All right reserved.
 * Contacts: dant.it@gmail.com, dokshin@list.ru.
 */
package mddvrextractor;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Реализация ридера для чтения обычных файлов (всё, что поддерживает ОС).
 *
 * @author Докшин Алексей Николаевич <dant.it@gmail.com>
 */
public class NativeFileReader implements NativeReader {

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
     *
     * @param name Дескриптор файла-источника.
     * @throws FileNotFoundException Ошибка при отсутствии файла.
     * @throws IOException Ошибка при позиционировании.
     */
    public NativeFileReader(String name) throws FileNotFoundException, IOException {
        this.name = name;
        in = null;
        if (name != null) {
            in = new RandomAccessFile(name, "r");
            in.seek(0);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getSize() throws IOException {
        return in.length();
    }

    @Override
    public void seek(long n) throws IOException {
        in.seek(n);
    }

    @Override
    public void skip(int n) throws IOException {
        while (n > 0) {
            n -= in.skipBytes(n);
        }
    }

    @Override
    public int read(byte[] ba, int index, int size) throws IOException {
        int readed = 1, pos = 0, n = 0;
        while (readed >= 0 && pos < size && readed != -1) {
            readed = in.read(ba, index + pos, size - pos);
            pos += readed;
            n += readed;
        }
        return n;
    }

    @Override
    public void close() throws IOException {
        if (in != null) {
            in.close();
        }
    }

    @Override
    public void closeSafe() {
        try {
            close();
        } catch (IOException ex) {
        }
    }
}
