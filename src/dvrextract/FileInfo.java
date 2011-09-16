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
    // Номера камер содержащиеся в файле
    // (для EXE - из инфы файла, для HDD - из первого фрейма).
    public ArrayList<Integer> camNumbers;
    // Распознанный первый кадр.
    public Frame frameFirst;
    // Распознанный последний кадр.
    public Frame frameLast;
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
    private static final Comparator<FileInfo> comparator = new Comparator<FileInfo>() {

        @Override
        public int compare(FileInfo o1, FileInfo o2) {
            long t1 = o1.frameFirst.time.getTime();
            long t2 = o2.frameFirst.time.getTime();
            return t1 == t2 ? 0 : (t1 < t2 ? -1 : 1);
        }
    };

    /**
     * Конструктор.
     */
    public FileInfo() {
        fileName = null;
        fileSize = 0;
        fileType = FileType.NO;
        camNumbers = new ArrayList<Integer>();
        frameFirst = null;
        frameLast = null;

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
        for (Integer i : camNumbers) {
            if (sb.length() != 0) {
                sb.append(", ");
            }
            sb.append(i);
        }
        return sb.toString();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Дочерние классы.
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Для учёта пропущенных кадров.
     */
    public class Skip {

        long time; // Время пропущенного кадра.
        long count; // Кол-во пропущенных кадров.
    }

    /**
     * Для учёта "битых" участков в файлах.
     */
    public class Error {

        long pos; // Позиция возникновения ошибки.
        long count; // Кол-во пропущенных байт.
    }
}