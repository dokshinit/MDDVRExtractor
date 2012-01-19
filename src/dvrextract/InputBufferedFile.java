/*
 * Copyright (c) 2011-2012, Aleksey Nikolaevich Dokshin. All right reserved.
 * Contacts: dant.it@gmail.com, dokshin@list.ru.
 */
package dvrextract;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import xfsengine.XFS.XFSException;

/**
 * Файл для буферизованного чтения с кешированием данных.
 * Позволяет возвращаться назад и читать в пределах размера кеша без обращения к диску.
 * Справедливо для любого направления движения, от начала к концу или наоборот.
 * @author Докшин Алексей Николаевич <dant.it@gmail.com>
 */
public class InputBufferedFile {

    /**
     * Имя файла.
     */
    private FileDesc desc;
    /**
     * Файл с произвольным доступом (из-за EXE и сканирования).
     */
    private NativeReader in;
    /**
     * Размер файла (считывается при инициализации).
     */
    private long fileSize;
    /**
     * Массив-приёмник считываемых данных.
     */
    private byte[] bufferArray;
    /**
     * Буфер для преобразования данных в массиве-приёмнике.
     */
    private ByteBuffer byteBuffer;
    /**
     * Массив-приёмник для данных атомарных типов.
     */
    private byte[] parseArray;
    /**
     * Буфер для преобразования данных из атомарных типов.
     */
    private ByteBuffer parseBuffer;
    /**
     * Сигнализатор отсутствия содержимого буфера.
     */
    private boolean isBufferEmpty;
    /**
     * Размер буфера.
     */
    private int bufferSize;
    /**
     * Размер глубины буфера при последовательных смещениях.
     * Т.е. при смещении буфера пред.данные в указанном размере остаются в буфере. 
     * Для оптимизации обащений к диску.
     */
    private int cacheSize;
    /**
     * Позиция начала буфера в файле.
     */
    private long bufferPosition;
    /**
     * Текущая логическая позиция в файле 
     * (не соответствует реальной, реального позиционирвания не происходит!)
     */
    private long position;

    /**
     * Конструктор.
     * При создании указывается размер буфера и размер сохраняемых данных при 
     * смещении буфера.
     * Например: 100кб буфер и 10кб кэш - при чтении >100кб буфер сдвинется на 
     * (100-10)кб при этом последние 10кб буфера сдвинутся в начало и 90кб 
     * дочитаются следом за ними. Т.е. смысл в том, чтобы если возникнет нужда
     * вернуть указатель назад и считать инфу заново, то в пределах размера кэша
     * это не вызовет обращения к файлу - будет читаться из буфера.
     * @param fileName Имя файла-источника.
     * @param bufsize Размер буфера в байтах.
     * @param cachesize Размер кеша в байтах.
     * @throws FileNotFoundException Ошибка при отсутствии файла.
     * @throws IOException Ошибка при позиционировании.
     */
    public InputBufferedFile(FileDesc desc, int bufsize, int cachesize) throws FileNotFoundException, IOException, XFSException {
        // Буфер парсинга данных.
        parseArray = new byte[16]; // byte/int/long/double (<= 8 bytes.)
        parseBuffer = ByteBuffer.wrap(parseArray);
        parseBuffer.order(ByteOrder.LITTLE_ENDIAN);
        this.desc = desc;
        if (desc != null) {
            if (desc.id == 0) {
                in = new NativeFileReader(desc.name);
            } else {
                in = new NativeXFSReader(desc.id, desc.name);
            }
            in.seek(0);
        }
        fileSize = in.getSize();
        bufferPosition = 0;
        position = 0;
        setBuffer(bufsize, cachesize);
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            in.close();
        } catch (Exception e) {
        }
        super.finalize();
    }

    /**
     * Возвращает размер файла.
     * @return Размер файла.
     */
    public long getSize() {
        return fileSize;
    }

    /**
     * Возвращает позицию текущего логическую указателя для чтения.
     * @return Позиция.
     */
    public long getPosition() {
        return position;
    }

    /**
     * Возвращает смещение в буфере для указанной позиции в файле.
     * @param pos Позиция в файле.
     * @return Позиция в буфере соответсвующая позиции в файле.
     * @throws IOException Ошибка ввода\вывода.
     */
    public int getBufferIndex(long pos) throws IOException {
        int bpos = (int) (pos - bufferPosition);
        if (bpos < 0 || bpos >= bufferSize) {
            throw new IOException("Out of buffer: bpos=" + bpos + " pos=" + pos);
        }
        return bpos;
    }

    /**
     * Возвращает байт-буфер для внешних операций с буфером.
     * Например - считывание данных напрямую из буфера (безопасно в пределах 
     * кеша т.к. данные заведомо есть!).
     * @return Байт-буфер.
     */
    public ByteBuffer getByteBuffer() {
        return byteBuffer;
    }

    /**
     * Установка размеров буфера и кэша. Не сбрасывает позицию!
     * @param size Размер буфера.
     * @param cache Размер кэша (д.б. меньше размера буфера!).
     * @throws IOException При ошибках позиционирования.
     */
    private void setBuffer(int size, int cache) throws IOException {
        if (size < 2048) {
            size = 2048;
        }
        if (cache < 0) {
            cache = 0;
        }
        if (size >= fileSize) {
            size = (int) fileSize;
            cache = 0;
        } else if (size - cache < 512) {
            cache = size - 512;
        }
        bufferSize = size;
        cacheSize = cache;

        bufferArray = new byte[size];
        byteBuffer = ByteBuffer.wrap(bufferArray);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

        isBufferEmpty = true;
        seek(getPosition());
    }

    /**
     * Чтение блока данных с текущей позиции в буфер.
     * @param ba Буфер.
     * @param index Начальная позиция в буфере.
     * @param size Размер данных в байтах.
     * @throws IOException Ошибка ввода-вывода.
     */
    private void readNative(byte[] ba, int index, int size) throws IOException {
        int readed = 1, pos = 0;
        while (readed >= 0 && pos < size) {
            readed = in.read(ba, index + pos, size - pos);
            pos += readed;
        }
    }

    /**
     * Актуализация блока данных в буфере. Исходя из текущей позиции буфера,
     * заданного смещения в буфере и размера блока.
     * @param index Начальная позиция блока в буфере.
     * @param size Размер данных в байтах.
     * @throws IOException Ошибка ввода-вывода.
     */
    private void updateBuffer(long pos, int index, int size) throws IOException {
        bufferPosition = pos;
        in.seek(bufferPosition + index);
        readNative(bufferArray, index, size);
    }

    /**
     * Перемещение заданого кол-ва байт из конца буфера в начало.
     * @param n Кол-во байт.
     */
    private void moveToStart(int n) {
        for (int i = 0, j = bufferSize - n; i < n; i++, j++) {
            bufferArray[i] = bufferArray[j];
        }
    }

    /**
     * Перемещение заданого кол-ва байт из начала буфера в конец.
     * @param n Кол-во байт.
     */
    private void moveToEnd(int n) {
        for (int i = n - 1, j = bufferSize - 1; i >= 0; i--, j--) {
            bufferArray[j] = bufferArray[i];
        }
    }

    /**
     * Актуализирует содержимое буфера согласно текущей позиции.
     * Если необходимо - происходит сдвиг буфера и обновление недостающих данных.
     * @throws IOException 
     */
    private void validateBuffer() throws IOException {
        if (position < bufferPosition) { // Двигаем буфер назад.
            // Новая позиция буфера.
            long newPos = position - (bufferSize - cacheSize);
            if (newPos < 0) { // Если меньше начала файла.
                newPos = 0;
            }
            // Кол-во байт в буфере которые перекрываются новым буфером.
            int nOld = (int) ((newPos + bufferSize) - bufferPosition);
            if (!isBufferEmpty && nOld > 0) { // Если буфер загружен и есть что смещать.
                moveToEnd(nOld);
                updateBuffer(newPos, 0, bufferSize - nOld);
            } else { // Если буфер не загружен или смещать нечего - читаем полностью.
                updateBuffer(newPos, 0, bufferSize);
            }

        } else if (position >= bufferPosition + bufferSize) { // Двигаем буфер вперёд.
            // Новая позиция буфера.
            long newPos = position - cacheSize;
            if (newPos + bufferSize > fileSize) { // Если вышли за длину файла.
                newPos = fileSize - bufferSize;
            }
            // Кол-во байт в буфере которые перекрываются новым буфером.
            int nOld = (int) ((bufferPosition + bufferSize) - newPos);
            if (!isBufferEmpty && nOld > 0) { // Если буфер загружен и есть что смещать.
                moveToStart(nOld);
                updateBuffer(newPos, nOld, bufferSize - nOld);
            } else { // Если буфер не загружен или смещать нечего - читаем полностью.
                updateBuffer(newPos, 0, bufferSize);
            }
        } else { // Попадаем в буфер.
            if (isBufferEmpty) { // Если буфер не загружен - загружаем.
                updateBuffer(bufferPosition, 0, bufferSize);
            }
        }
        isBufferEmpty = false;
    }

    /**
     * Проверяет находятся ли данные по текущей позиции в буфере.
     * @return ture - в буфере, false - нет (в том числе, когда буфер пуст).
     */
    private boolean isByteBuffered() {
        return (!isBufferEmpty && position >= bufferPosition && position < bufferPosition + bufferSize) ? true : false;
    }

    /**
     * Возвращает байт с текущей позиции (НЕ инкрементирует позицию!)
     * @return Значение (байт).
     * @throws IOException При ошибках позиционирования и валидации буфера.
     */
    private byte getBufferedByte() throws IOException {
        if (!isByteBuffered()) {
            validateBuffer();
        }
        long bufpos = position - bufferPosition;
        if (bufpos < 0 || bufpos >= bufferSize) {
            throw new IOException("Out of buffer: index=" + bufpos
                    + " pos=" + position + " buf=" + bufferPosition);
        }
        return bufferArray[(int) bufpos];
    }

    /**
     * Позиционирует текущий логический указатель чтения/записи на заданную 
     * позицию от начала файла. Валидации буфера не происходит (делается при 
     * первом чтении).
     * @param pos Позиция.
     * @throws IOException Ошибка ввода-вывода при выходе за диапазон позиций.
     */
    public void seek(long pos) throws IOException {
        if (pos < 0 || pos > fileSize) {
            throw new IOException("Out of file: pos=" + pos + " size=" + fileSize);
        }
        position = pos;
    }

    /**
     * Буферизированное считывание данных в массив с тек.позиции.
     * @param ba Приёмник.
     * @param index Начальный отступ в приёмнике.
     * @param size Размер считываемого блока.
     * @throws IOException Ошибка ввода\вывода.
     */
    public void read(byte[] ba, int index, int size) throws IOException {
        // Для первого варианта реализовал упрощенный вариант.
        for (int i = 0; i < size; i++) {
            ba[index + i] = getBufferedByte();
            seek(position + 1);
        }
    }

    /**
     * Буферизированное считывание данных в массив с нулевого индекса с тек.позиции.
     * @param ba Приёмник.
     * @param size Размер считываемого блока.
     * @throws IOException Ошибка ввода\вывода.
     */
    public void read(byte[] ba, int size) throws IOException {
        read(ba, 0, size);
    }

    /**
     * Пропуск указанного кол-ва байт от текущей позиции (смещение позиции на
     * указанное кол-во байт).
     * @param n Смещение в байтах.
     * @throws IOException Ошибка ввода-вывода.
     */
    public void skip(long n) throws IOException {
        seek(position + n);
    }

    /**
     * Закрытие файла-источника.
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

    /**
     * Чтение byte из файла (буферизированно). Указатель смещается.
     * @return Считанное значение.
     * @throws IOException Ошибка операции.
     */
    public byte readByte() throws IOException {
        byte b = getBufferedByte();
        seek(position + 1);
        return b;
    }

    /**
     * Чтение short из файла (буферизированно). Указатель смещается.
     * @return Считанное значение.
     * @throws IOException Ошибка операции.
     */
    public short readShort() throws IOException {
        read(parseArray, 0, 2);
        return parseBuffer.getShort(0);
    }

    /**
     * Чтение int из файла (буферизированно). Указатель смещается.
     * @return Считанное значение.
     * @throws IOException Ошибка операции.
     */
    public int readInt() throws IOException {
        read(parseArray, 0, 4);
        return parseBuffer.getInt(0);
    }

    /**
     * Чтение long из файла (буферизированно). Указатель смещается.
     * @return Считанное значение.
     * @throws IOException Ошибка операции.
     */
    public long readLong() throws IOException {
        read(parseArray, 0, 8);
        return parseBuffer.getLong(0);
    }

    /**
     * Чтение float из файла (буферизированно). Указатель смещается.
     * @return Считанное значение.
     * @throws IOException Ошибка операции.
     */
    public float readFloat() throws IOException {
        read(parseArray, 0, 4);
        return parseBuffer.getFloat(0);
    }

    /**
     * Чтение double из файла (буферизированно). Указатель смещается.
     * @return Считанное значение.
     * @throws IOException Ошибка операции.
     */
    public double readDouble() throws IOException {
        read(parseArray, 0, 8);
        return parseBuffer.getDouble(0);
    }

    /**
     * Чтение byte из файла с указанной позиции (буферизированно). Указатель смещается.
     * @param pos Позиция.
     * @return Считанное значение.
     * @throws IOException Ошибка операции.
     */
    public byte readByte(long pos) throws IOException {
        seek(pos);
        return readByte();
    }

    /**
     * Чтение short из файла с указанной позиции (буферизированно). Указатель смещается.
     * @param pos Позиция.
     * @return Считанное значение.
     * @throws IOException Ошибка операции.
     */
    public short readShort(long pos) throws IOException {
        seek(pos);
        return readShort();
    }

    /**
     * Чтение int из файла с указанной позиции (буферизированно). Указатель смещается.
     * @param pos Позиция.
     * @return Считанное значение.
     * @throws IOException Ошибка операции.
     */
    public int readInt(long pos) throws IOException {
        seek(pos);
        return readInt();
    }

    /**
     * Чтение long из файла с указанной позиции (буферизированно). Указатель смещается.
     * @param pos Позиция.
     * @return Считанное значение.
     * @throws IOException Ошибка операции.
     */
    public long readLong(long pos) throws IOException {
        seek(pos);
        return readLong();
    }

    /**
     * Чтение float из файла с указанной позиции (буферизированно). Указатель смещается.
     * @param pos Позиция.
     * @return Считанное значение.
     * @throws IOException Ошибка операции.
     */
    public float readFloat(long pos) throws IOException {
        seek(pos);
        return readFloat();
    }

    /**
     * Чтение double из файла с указанной позиции (буферизированно). Указатель смещается.
     * @param pos Позиция.
     * @return Считанное значение.
     * @throws IOException Ошибка операции.
     */
    public double readDouble(long pos) throws IOException {
        seek(pos);
        return readDouble();
    }
}
