/*
 * Copyright (c) 2011-2012, Aleksey Nikolaevich Dokshin. All right reserved.
 * Contacts: dant.it@gmail.com, dokshin@list.ru.
 */
package dvrextract;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Сохранение сырых данных в файл.
 * @author Докшин Алексей Николаевич <dant.it@gmail.com>
 */
public class OutputFile {

    /**
     * Имя файла.
     */
    private String name;
    /**
     * Поток файла.
     */
    private OutputStream fout;

    /**
     * Конструктор.
     * @param fileName Имя файла.
     * @throws FileNotFoundException Ошибка при отсутствии файла.
     */
    public OutputFile(String fileName) throws FileNotFoundException {
        name = fileName;
        fout = null;
        if (name != null) {
            File ff = new File(name);
            if (ff.exists()) {
                ff.delete();
            }
            fout = new BufferedOutputStream(new FileOutputStream(name));
        }
    }

    /**
     * Запись данных из буфера в файл.
     * @param ba Буфер данных.
     * @param offset Смещение в буфере (в байтах).
     * @param size Размер данных для записи (в байтах).
     * @throws IOException Ошибка при операции записи.
     */
    public void write(byte[] ba, int offset, int size) throws IOException {
        if (fout != null) {
            fout.write(ba, offset, size);
        }
    }

    /**
     * Закрытие файла с сохранением буферизированных данных.
     * @throws IOException Ошибка при операции записи.
     */
    public void close() throws IOException {
        if (fout != null) {
            fout.flush();
            fout.close();
        }
    }

    /**
     * Безопасный вариант закрытия файла (не вызывает исключений).
     */
    public void closeSafe() {
        try {
            close();
        } catch (IOException ex) {
        }
    }
}
