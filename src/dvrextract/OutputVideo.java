package dvrextract;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Запись видео в файл (конвертирует FFMpeg).
 * @author lex
 */
public class OutputVideo {

    // Имя файла.
    private String name;
    // Поток файла.
    private FileOutputStream fout;
    // Процесс конвертации.
    private Process process;
    private InputStream processIn;
    private OutputStream processOut;
    // Готовые опции преобразования.
    private String videoOptions;
    private String audioOptions;
    private int subMode;

    /**
     * Конструктор.
     * @param fileName Имя файла.
     * @throws FileNotFoundException Ошибка при отсутствии файла.
     */
    public OutputVideo(String fileName, String vopt, String aopt, int smode) throws FileNotFoundException {
        videoOptions = vopt;
        audioOptions = aopt;
        subMode = smode;

        name = fileName;
        fout = null;
        if (name != null) {
            File ff = new File(name);
            if (ff.exists()) {
                ff.delete();
            }
            fout = new FileOutputStream(name);
        }
        // Стартуем процесс FFMpeg.
        process = null;
        processIn = null;
        processOut = null;
        try {
            process = Runtime.getRuntime().exec("ffmpeg -i - " + videoOptions + " " + audioOptions + " -");
            processIn = process.getInputStream();
            processOut = process.getOutputStream();
        } catch (IOException ex) {
            if (process != null) {
                process.destroy();
            }
        }
    }

    public void convert(byte[] ba, int offset, int size) throws IOException {
        // Запись в процесс фрейма.
        processOut.write(ba, offset, size);
    }
    
    private byte[] buf = new byte[100000];

    /**
     * Если есть данные для записи - считываем из процесса и записываем в файл.
     * @throws IOException Ошибка при операции записи.
     */
    public void save() throws IOException {
        int size = processIn.available();
        while (size > 0) {
            // Считываем.
            int len = Math.min(buf.length, size);
            int readed = 1, pos = 0;
            while (readed >= 0 && pos < len) {
                readed = processIn.read(buf, pos, size - pos);
                pos += readed;
            }
            // Записываем в файл.
            if (fout != null) {
                fout.write(buf, 0, len);
            }
            size -= len;
        }
    }

    public void finish() throws IOException {
        if (processOut != null) {
            processOut.flush();
            processOut.close();
            processOut = null;
        }
        save();
    }
    
    /**
     * Закрытие файла с сохранением буферизированных данных.
     * @throws IOException Ошибка при операции записи.
     */
    public void close() throws IOException {
        finish();
        
        if (process != null) {
            process.destroy();
        }
        if (fout != null) {
            fout.flush();
            fout.close();
        }
    }
}
