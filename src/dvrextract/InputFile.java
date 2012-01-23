/*
 * Copyright (c) 2011-2012, Aleksey Nikolaevich Dokshin. All right reserved.
 * Contacts: dant.it@gmail.com, dokshin@list.ru.
 */
package dvrextract;

import java.io.FileNotFoundException;
import java.io.IOException;
import xfsengine.XFS.XFSException;

/**
 * Низкоуровневые операции с файлом-источником.
 *
 * @author Докшин Алексей Николаевич <dant.it@gmail.com>
 */
public class InputFile {

    /**
     * Имя файла.
     */
    private FileDesc desc;
    /**
     * Файл с произвольным доступом (из-за EXE и сканирования).
     */
    private NativeReader in;

    /**
     * Конструктор.
     *
     * @param fileDesc Имя файла-источника.
     * @throws FileNotFoundException Ошибка при отсутствии файла.
     * @throws IOException Ошибка при позиционировании.
     * @throws XFSException Ошибка XFS.
     */
    public InputFile(FileDesc fileDesc) throws FileNotFoundException, IOException, XFSException {
        if (fileDesc == null || fileDesc.name == null) {
            throw new IOException("Filename is null!");
        }
        desc = fileDesc;
        switch (desc.type) {
            case FileDesc.FS:
                in = new NativeFileReader(desc.name);
                break;
            case FileDesc.XFS:
                in = new NativeXFSReader(desc.name);
                break;
        }
        in.seek(0);
    }

    @Override
    protected void finalize() throws Throwable {
        in.closeSafe();
        super.finalize();
    }

    /**
     * Возвращает размер файла.
     *
     * @return Размер файла.
     * @throws IOException Ошибка ввода-вывода.
     */
    public long getSize() throws IOException {
        return in.getSize();
    }

    /**
     * Позиционирует текущий указатель чтения/записи на заданную позицию от
     * начала файла.
     *
     * @param n Позиция.
     * @throws IOException Ошибка ввода-вывода.
     */
    public void seek(long n) throws IOException {
        in.seek(n);
    }

    /**
     * Пропуск указанного кол-ва байт от текущей позиции (смещение позиции на
     * указанное кол-во байт).
     *
     * @param n Смещение в байтах.
     * @throws IOException Ошибка ввода-вывода.
     */
    public void skip(int n) throws IOException {
        in.skip(n);
    }

    /**
     * Чтение блока данных с текущей позиции в буфер (с начала).
     *
     * @param ba Буфер.
     * @param size Размер данных в байтах.
     * @throws IOException Ошибка ввода-вывода.
     */
    public void read(byte[] ba, int size) throws IOException {
        in.read(ba, 0, size);
    }

    /**
     * Закрытие файла-источника.
     *
     * @throws IOException Ошибка ввода-вывода.
     */
    public void close() throws IOException {
        in.close();
    }

    /**
     * Безопасный вариант закрытия файла-источника (не вызывает исключений).
     */
    public void closeSafe() {
        in.closeSafe();
    }
}
