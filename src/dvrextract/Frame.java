package dvrextract;

import java.awt.Dimension;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.TimeZone;

/**
 * Работа с кадрами источника.
 * TODO: Скрыть поля и сделать методы только на чтение? (кроме pos...)
 * @author lex
 */
public class Frame {

    // Смещение времени по зонам.
    private static long timeZone = TimeZone.getDefault().getRawOffset() + 3600000;
    //
    // Поля:
    // Дата-время (смещение в секундах от 1970 г.)
    public Date time;
    // Номер камеры (+допинфа в старшем байте? игнорируем).
    public int camNumber;
    // Код разрешениея: 4-352x288, 5-704x288, 6-704x576, 8-1280x720, 9-1920x1080.
    public int idResolution;
    // Частота кадров в секунду.
    public int fps;
    // Базовый кадр.
    public boolean isMain;
    // Размер кадра видеоданных.
    public int videoSize;
    // Размер кадра аудиоданных.
    public int audioSize;
    // Номер кадра.
    public int number;
    //
    // Флаг успешного разбора кадра.
    public boolean isParsed;
    // Позиция начала в файле.
    public long pos;
    // Тип файла фрейма.
    public FileType type;

    /**
     * Конструктор.
     */
    public Frame(FileType type) {
        time = null;
        camNumber = 0;
        fps = 0;
        isMain = false;
        videoSize = -1;
        audioSize = -1;
        number = -1;

        isParsed = false;
        pos = -1;
        this.type = type;
    }

    /**
     * Возвращает размер заголовка для данного типа фрейма.
     * @return Размер в байтах.
     */
    public int getHeaderSize() {
        switch (type) {
            case EXE:
                // Размер заголовка фрейма в архивных файлах выгрузки.
                return 93;
            case HDD:
                // Размер заголовка фрейма в файлах на HDD.
                return 77;
            default:
                return 0;
        }
    }

    /**
     * Проверяет является ли код разрешения видеокадра допустимым.
     * @return true - да, false - неизвестный код.
     */
    public boolean isValidResolution() {
        if (idResolution == 4 || idResolution == 5 || idResolution == 6
                || idResolution == 8 || idResolution == 9) {
            return true;
        }
        return false;
    }

    /**
     * Возвращает разрешение видеокадра (или разрешение по умолчанию, если фрейм
     * не распознанн или неизвестный код разрешения).
     * @return 
     */
    public Dimension getResolution() {
        switch (idResolution) {
            case 4:
                return new Dimension(352, 288);
            case 5:
                return new Dimension(704, 288);
            case 6:
                return new Dimension(704, 576);
            case 8:
                return new Dimension(1280, 720);
            case 9:
                return new Dimension(1920, 1080);
        }
        return new Dimension(704, 576);
    }

    /**
     * Установка зонального смещения (часовой пояс отн.мирового).
     * @param msec Новое смещение в миллисекундах).
     */
    public static void setZoneShift(long msec) {
        timeZone = msec;
    }

    /**
     * Возвращает зональное смещение в миллисекундах.
     * @return Смещение в миллисекундах.
     */
    public static long getZoneShift() {
        return timeZone;
    }

    /**
     * Преобразование даты регистратора в дату Java.
     * Дата хранится в формате int - кол-во секунд от 01.01.1970.
     * @return Дата и время в формате джавы.
     */
    public static Date dateFromRAW(int bdate) {
        // Часовой пояс для вычисления коррекции к мировому времени.
        // Приведение к дате в счислении Java: кол-во мс от 01.01.1970.
        // отнимаем 60 минут чтобы компенсировать ленее время - ПРОКОНТРОЛИРОВАТЬ НА СТЫКЕ!
        long javaDate = (long) bdate * 1000;
        // Внесение коррекции на часовой пояс.
        // НЕ учитывается переход на летнее и зимнее время, т.к. исходная
        // информация уже с учётом летнего времени!
        return new Date(javaDate - timeZone);
    }

    /**
     * Обратное преобразование в время DVR.
     * @param date Дата.
     * @return Время в формате DVR.
     */
    public static int dateToRAW(Date date) {
        return (int) ((date.getTime() + timeZone) / 1000);
    }

    /**
     * Распарсивает буфер заголовка, если успешно - в переменных фрейма - значения.
     * @return 0 - успешно, иначе - код ошибки (>0).
     */
    public int parseHeader(ByteBuffer bb, int offset) {
        isParsed = false;

        int nameofs = 0;
        switch (type) {
            case EXE:
                nameofs = 0x41;
                break;
            case HDD:
                nameofs = 0x31;
                break;
            default:
                return 100;
        }

        int b1 = bb.get(offset + nameofs);
        int b2 = bb.get(offset + nameofs + 1);
        int b3 = bb.get(offset + nameofs + 2);
        if (b1 != 'C' || b2 != 'A' || b3 != 'M') {
            return 1;
        }
        // Номер камеры (+допинфа в старшем байте? игнорируем).
        camNumber = (bb.getInt(offset + 0x8) & 0xFF) + 1;
        if (camNumber < 1 || camNumber > App.MAXCAMS) {
            return 2;
        }
        // Код разрешения видеокадра.
        idResolution = bb.get(offset + 0x10);
        if (idResolution < 0 || idResolution > 100) {
            return 3;
        }
        // Количество кадров в секунду.
        fps = bb.get(offset + 0x12);
        if (fps < 0 || fps > 60) {
            return 4;
        }
        // Флаг ключевого (базового) кадра.
        int mf = bb.get(offset + 0x13);
        if (mf > 1) { // MainFrame
            return 5;
        }
        // Размер кадра видеоданных.
        videoSize = bb.getInt(offset + 0x19);
        if (videoSize < 0 || videoSize > 1000000) {
            return 6;
        }
        // Размер кадра аудиоданных.
        audioSize = bb.getInt(offset + 0x1D);
        if (audioSize < 0 || audioSize > 1000000) {
            return 7;
        }
        // Дата и время кадра.
        int tb = bb.getInt(offset + 0x04);
        if (tb < 1104541200) {
            return 8;
        }
        // Дата-время (смещение в секундах от 1970 г.)
        time = dateFromRAW(tb);
        // Номер кадра.
        number = bb.getInt(offset + 0x2D);
        // Базовый кадр.
        isMain = (mf == 0) ? true : false;
        // Позиция вычисляется позже.
        pos = -1;
        isParsed = true;
        return 0;
    }
}
