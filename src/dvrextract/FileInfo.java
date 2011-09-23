package dvrextract;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Информация о файле-источнике 
 * (заполняется при сканировании, дополняется при обработке).
 * @author lex
 */
public final class FileInfo {
    
    ////////////////////////////////////////////////////////////////////////////
    // Информация о файле при сканировании.
    ////////////////////////////////////////////////////////////////////////////
    // Имя файла.
    public String fileName;
    // Размер файла.
    public long fileSize;
    // Тип файла (1-exe, 2-hdd).
    public FileType fileType;
    // Информация о камерах содержащаяся в файле
    // (для EXE - из инфы файла, для HDD - из первого фрейма).
    public ArrayList<CamData> camInfo;
    // Распознанный первый кадр файла (не камеры!).
    public Frame frameFirst;
    // Распознанный последний кадр файла (не камеры!).
    public Frame frameLast;
    // Начало видеоданных в файле.
    public long startDataPos;
    // Конец видеоданных в файле.
    public long endDataPos;
    ////////////////////////////////////////////////////////////////////////////
    // Заполняются при обработке:
    ////////////////////////////////////////////////////////////////////////////
    // Кол-во распарсеных кадров.
    public long p_ParsedCount;
    // Кол-во обработанных кадров.
    public long p_Count;
    // Размер обработанных данных (без заголовков).
    public long p_Size;
    // Обработанный первый кадр.
    public Frame p_First;
    // Обработанный последний кадр.
    public Frame p_Last;
    // Минимальное и максимальное время среди обработанных кадров.
    public long p_MinTime, p_MaxTime;
    // Пропуски кадров (для оценки регистратора).
    public ArrayList<Skip> p_Skip;
    // Пропуски из-за нарушения файла.
    public ArrayList<Error> p_Error;
    ////////////////////////////////////////////////////////////////////////////
    // Для интерфейса.
    ////////////////////////////////////////////////////////////////////////////
    // Компаратор для сортировки списка файлов.
    private static final Comparator<FileInfo> comparator = new FileInfoComparator();

    /**
     * Конструктор.
     */
    public FileInfo() {
        fileName = null;
        fileSize = 0;
        fileType = FileType.NO;
        camInfo = new ArrayList<CamData>();
        frameFirst = null;
        frameLast = null;
        startDataPos = 0;
        endDataPos = 0;
        
        p_ParsedCount = 0;
        p_Count = 0;
        p_Size = 0;
        p_First = null;
        p_Last = null;
        p_MinTime = 0;
        p_MaxTime = 0;
        p_Skip = new ArrayList<Skip>();
        p_Error = new ArrayList<Error>();
    }
    
    /**
     * Добавляет инфу о камере в список камер файла.
     * @param num Номер камеры.
     * @param offs Отступ первого фрейма.
     * @param f Фрейм (если распознан).
     * @return Инфа по камере.
     */
    public CamData addCamData(int num, long offs, Frame f) {
        CamData i = new CamData(num, offs, f);
        camInfo.add(i);
        return i;
    }
    
    /**
     * Возвращает инфу по камере файла.
     * @param num Номер камеры.
     * @return Инфа по камере (если нет = null).
     */
    public CamData getCamData(int num) {
        for (CamData i : camInfo) {
            if (i.camNumber == num) {
                return i;
            }
        }
        return null;
    }

    /**
     * Возвращает компаратор для сортровки списка из FileInfo.
     * @return Компаратор.
     */
    public static Comparator<FileInfo> getComparator() {
        return comparator;
    }

    /**
     * Возвращает список номеров камер как строку с их перечислением через запятую.
     * @return Строка со списком номеров камер.
     */
    public String getCamsToString() {
        StringBuilder sb = new StringBuilder();
        for (CamData i : camInfo) {
            if (sb.length() != 0) {
                sb.append(", ");
            }
            sb.append(i.camNumber);
        }
        return sb.toString();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Дочерние классы.
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Для хранения отступов первых фреймов.
     */
    public class CamData {
        // Номер камеры.
        public int camNumber;
        // Отступ в файле до первого ключевого кадра камеры.
        public long mainFrameOffset; 
        // Первый базовый кадр камеры (если=null - значит еще не распознан).
        public Frame mainFrame;
        
        public CamData(int num, long offs, Frame f) {
            camNumber = num;
            mainFrameOffset = offs;
            mainFrame = f;
        }
    }
 
    /**
     * Для учёта пропущенных кадров.
     */
    public class Skip {

        public long time; // Время пропущенного кадра.
        public long count; // Кол-во пропущенных кадров.
    }

    /**
     * Для учёта "битых" участков в файлах.
     */
    public class Error {

        public long pos; // Позиция возникновения ошибки.
        public long count; // Кол-во пропущенных байт.
    }

    /**
     * Реализация компаратора для инфы файла.
     */
    private static class FileInfoComparator implements Comparator<FileInfo> {

        @Override
        public int compare(FileInfo o1, FileInfo o2) {
            long t1 = o1.frameFirst.time.getTime();
            long t2 = o2.frameFirst.time.getTime();
            return t1 == t2 ? 0 : (t1 < t2 ? -1 : 1);
        }
    }
}