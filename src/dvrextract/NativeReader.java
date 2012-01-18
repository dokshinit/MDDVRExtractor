/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dvrextract;

import java.io.IOException;

/**
 *
 * @author lex
 */
public interface NativeReader {

    /**
     * Возвращает полное имя файла.
     * @return Имя файла.
     */
    public String getName();

    /**
     * Возвращает размер файла.
     * @return Размер файла.
     * @throws IOException Ошибка ввода-вывода.
     */
    public long getSize() throws IOException;

    /**
     * Позиционирует текущий указатель чтения/записи на заданную позицию от
     * начала файла.
     * @param n Позиция.
     * @throws IOException Ошибка ввода-вывода.
     */
    public void seek(long pos) throws IOException;

    /**
     * Пропуск указанного кол-ва байт от текущей позиции (смещение позиции на
     * указанное кол-во байт).
     * @param n Смещение в байтах.
     * @throws IOException Ошибка ввода-вывода.
     */
    public void skip(int n) throws IOException;

    /**
     * Чтение блока данных с текущей позиции в буфер (с начала).
     * @param ba Буфер.
     * @param size Размер данных в байтах.
     * @throws IOException Ошибка ввода-вывода.
     */
    public int read(byte[] buffer, int index, int size) throws IOException;

    /**
     * Закрытие файла-источника.
     * @throws IOException Ошибка ввода-вывода.
     */
    public void close() throws IOException;

    /**
     * Безопасный вариант закрытия файла-источника (не вызывает исключений).
     */
    public void closeSafe();
}
