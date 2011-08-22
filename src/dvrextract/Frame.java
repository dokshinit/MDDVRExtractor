/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dvrextract;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 * @author lex
 */
public class Frame {
    public static final int ARCH_HSIZE = 93; // Размер заголовка фрейма в архивных файлах выгрузки.
    public static final int HDD_HSIZE = 77; // Размер заголовка фрейма в файлах на HDD.
    
    public Date time; // Дата-время (смещение в секундах от 1970 г.)
    public int camNumber; // Номер камеры (+допинфа в старшем байте? игнорируем).
    public int fps; // Чстота кадров в секунду.
    public boolean isMainFrame; // Базовый кадр.
    public int videoSize; // Размер кадра видеоданных.
    public int audioSize; // Размер кадра аудиоданных.
    public int number; // Номер кадра.
    public long pos; // Позиция начала в файле.
    public boolean isParsed;

    public Frame() {
        this.time = null;
        this.camNumber = 0;
        this.fps = 0;
        this.isMainFrame = false;
        this.videoSize = -1;
        this.audioSize = -1;
        this.number = -1;
        this.pos = -1;
        this.isParsed = false;
    }
    
    /**
     * Преобразование даты регистратора в дату Java.
     * Дата хранится в формате int - кол-во секунд от 01.01.1970.
     * @return Дата и время в формате джавы или null при ошибке.
     */
    private Date getDate(int bdate) {
        try {
            // Часовой пояс для вычисления коррекции к мировому времени.
            TimeZone curZone = TimeZone.getDefault();
            // Приведение к дате в счислении Java: кол-во мс от 01.01.1970.
            // отнимаем 60 минут чтобы компенсировать ленее время - ПРОКОНТРОЛИРОВАТЬ НА СТЫКЕ!
            long javaDate = (long) bdate * 1000 - 3600000;
            // Внесение коррекции на часовой пояс.
            // НЕ учитывается переход на летнее и зимнее время, т.к. исходная
            // информация уже с учётом летнего времени!
            return new Date(javaDate - curZone.getRawOffset());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Распарсивает буфер заголовка, если успешно - в переменных фрейма - значения.
     * @return 0 - успешно, иначе - код ошибки (>0).
     */
    public int parseHeader(ByteBuffer bb, int offset) {
        isParsed = false;
        
        int b1 = bb.get(offset + 0x31);
        int b2 = bb.get(offset + 0x32);
        int b3 = bb.get(offset + 0x33);
        if (b1 != 'C' || b2 != 'A' || b3 != 'M') {
            return 1;
        }
        // Номер камеры (+допинфа в старшем байте? игнорируем).
        camNumber = (bb.getInt(offset + 0x8) & 0xFF) + 1;
        if (camNumber < 1 || camNumber > App.MAXCAMS) {
            return 2;
        }
        fps = bb.get(offset + 0x12);
        if (fps < 0 || fps > 30) {
            return 3;
        }
        int mf = bb.get(offset + 0x13);
        if (mf > 1) { // MainFrame
            return 4;
        }
        videoSize = bb.getInt(offset + 0x19); // Размер кадра видеоданных.
        if (videoSize < 0 || videoSize > 1000000) {
            return 5;
        }
        audioSize = bb.getInt(offset + 0x1D); // Размер кадра аудиоданных.
        if (audioSize < 0 || audioSize > 1000000) {
            return 6;
        }
        number = bb.getInt(offset + 0x2D); // Номер кадра.
        time = getDate(bb.getInt(offset + 0x04)); // Дата-время (смещение в секундах от 1970 г.)
        isMainFrame = (mf == 0) ? true : false; // Базовый кадр.
        pos = -1;
        isParsed = true;
        return 0;
    }
}
