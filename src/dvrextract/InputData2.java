package dvrextract;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Низкоуровневые операции с файлом-источником.
 * @author lex
 */
public class InputData2 {

    // Имя файла.
    private String name;
    // Файл с произвольным доступом (из-за EXE и сканирования).
    private RandomAccessFile in;
    // Размер файла (считывается при инициализации).
    private long fileSize;
    // Буфер чтения и парсинга данных.
    private byte[] bufferArray;
    // Буфер для парсинга данных (может не нужен?).
    private ByteBuffer byteBuffer;
    // Сигнализатор отсутствия содержимого буфера.
    private boolean isBufferEmpty;
    // Размер буфера.
    private int bufferSize;
    // Размер глубины буфера при последовательных смещениях.
    // Т.е. при смещении буфера пред.данные в указанном размере остаются в буфере. Для оптимизации обащений к диску.
    private int cacheSize;
    // Позиция начала буфера в файле.
    private long bufferPosition; // Отступ начала буфера в файле.
    // Текущая логическая позиция в файле 
    // (не соответствует реальной, реального позиционирвания не происходит!)
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
     * @throws FileNotFoundException Ошибка при отсутствии файла.
     * @throws IOException Ошибка при позиционировании.
     */
    public InputData2(String fileName, int bufsize, int cachesize) throws FileNotFoundException, IOException {
        // Буфер чтения и парсинга данных.
        name = fileName;
        in = new RandomAccessFile(name, "r");
        fileSize = in.length();
        bufferPosition = 0;
        position = 0;
        setBuffer(bufsize, cachesize);
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
     * Чтение блока данных с текущей позиции в буфер (с начала).
     * @param ba Буфер.
     * @param size Размер данных в байтах.
     * @throws IOException Ошибка ввода-вывода.
     */
    private void readLow(byte[] ba, int index, int size) throws IOException {
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
        readLow(bufferArray, index, size);
    }

    private void moveToStart(int n) {
        for (int i = 0, j = bufferSize - n; i < n; i++, j++) {
            bufferArray[i] = bufferArray[j];
        }
    }

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
    }

    private boolean isPosInBuffer(long pos) {
        return (pos >= bufferPosition && pos < bufferPosition + bufferSize) ? true : false;
    }

    private byte getFromBuffer() throws IOException {
        long bufpos = position - bufferPosition;
        if (bufpos < 0 || bufpos >= bufferSize) {
            throw new IOException("bufpos=" + bufpos);
        }
        return bufferArray[(int)bufpos];
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
            throw new IOException("seek pos=" + pos);
        }
        position = pos;
    }

    /**
     * Буферизированное считывание данных в массив с тек.позиции.
     * @param ba Приёмник.
     * @param index Начальный отступ в приёмнике.
     * @param size Размер считываемого блока.
     */
    public void read(byte[] ba, int index, int size) throws IOException {
        // Для первого варианта реализовал упрощенный вариант.
        // Проверка на сущ.в буфере тек.позиции, если надо - актуализация буфера, 
        // далее - чтение из буфер байта.
        for (int i = 0; i < size; i++) {
            if (!isPosInBuffer(position)) {
                validateBuffer();
            }
            ba[index + i] = getFromBuffer();
            seek(position+1);
        }
    }

    /**
     * Пропуск указанного кол-ва байт от текущей позиции (смещение позиции на
     * указанное кол-во байт).
     * @param n Смещение в байтах.
     * @throws IOException Ошибка ввода-вывода.
     */
    public void skip(int n) throws IOException {
        seek(position + n);
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
}
