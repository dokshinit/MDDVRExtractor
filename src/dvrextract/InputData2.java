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

    // TODO: Сделать источник буферизированным на чтение
    // При создании указывается размер буфера и процент сдвига при выходе за буфер.
    // Например: 100кб буфер и при чтении во второй половине (>50%) +50% дочитывается.
    // или как >75% -> +75%. Как правило это размер на который необходимо 
    // осуществлять предчтение (возвращаться назад).
    /**
     * Конструктор.
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

    public long getPosition() {
        return position;
    }

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

    private void moveToEnd(int n) {
        for (int i = n - 1, j = bufferSize - 1; i >= 0; i--, j--) {
            bufferArray[j] = bufferArray[i];
        }
    }

    private void moveToStart(int n) {
        for (int i = 0, j = bufferSize - n; i < n; i++, j++) {
            bufferArray[i] = bufferArray[j];
        }
    }

    /**
     * Позиционирует текущий логический указатель чтения/записи на заданную 
     * позицию от начала файла.
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
     * Актуализирует содержимое буфера согласно текущей позиции.
     * Если необходимо - происходит сдвиг буфера и обновление недостающих данных.
     * @throws IOException 
     */
    private void validateBuffer() throws IOException {
        if (position < bufferPosition) {
            // Двигаем буфер назад.
            long newPos = position - (bufferSize - cacheSize);
            if (newPos < 0) {
                newPos = 0;
            }
            int nOld = (int) ((newPos + bufferSize) - bufferPosition);
            if (!isBufferEmpty && nOld > 0) { // Если есть что смещать.
                moveToEnd(nOld);
                updateBuffer(newPos, 0, bufferSize - nOld);
            } else {
                updateBuffer(newPos, 0, bufferSize);
            }

        } else if (position >= bufferPosition + bufferSize) {
            // Двигаем буфер вперёд.
            long newPos = position - cacheSize;
            if (newPos + bufferSize > fileSize) {
                newPos = fileSize - bufferSize;
            }
            int nOld = (int) ((bufferPosition + bufferSize) - newPos);
            if (!isBufferEmpty && nOld > 0) {
                moveToStart(nOld);
                updateBuffer(newPos, nOld, bufferSize - nOld);
            } else {
                updateBuffer(newPos, 0, bufferSize);
            }
        } else {
            // Попадаем в буфер.
            if (isBufferEmpty) {
                updateBuffer(bufferPosition, 0, bufferSize);
            }
        }
    }

    /**
     * Буферизированное считывание данных в массив с тек.позиции.
     * @param ba Приёмник.
     * @param index Начальный отступ в приёмнике.
     * @param size Размер считываемого блока.
     */
    public void read(byte[] ba, int index, int size) {
        // Если буфер не загружен - загружаем при первом же буф.действии.
        // Сливаем ту чать, что есть в буфере.
        long bsize = position
        
        if (size <= bufferSize) {
            // Обычное буферизированное чтение.
        } else {
            // Небуферизированное чтение + буф.чтение.
        }
        
        // Проверяем, где начало буфера.
        if (position >= bufferPosition && position < bufferPosition + bufferSize) {
            // Начало блока в буфере.
        } else {
            // Начало блока вне буфера.
        }

        // Если это не всё - определяем размер небуферизированного чтения
        // (если остаток больше размера буфера-шаг - т.е. все равно буфер тут же 
        // шагнёт дальше - избегаем лишних телодвижений). Считываем напрямую в 
        // приёмник.

        // Остаток считываем буферизированно подвинув буфер.
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
