package dvrextract;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Сохранение сырых данных в файл.
 * @author lex
 */
public class OutputFile {

    // Имя файла.
    private String name;
    // Поток файла.
    private FileOutputStream fout;

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
            fout = new FileOutputStream(name);
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
}
