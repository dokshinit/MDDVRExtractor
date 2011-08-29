/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dvrextract;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;

/**
 *
 * @author lex
 */
public class FileInfo {

    public String fileName; // Имя файла.
    public long fileSize;   // Размер файла.
    public int fileType;    // Тип файла (1-exe, 2-hdd).
    public ArrayList<Integer> camNumbers; // Номера камер содержащиеся в файле.
    public Frame frameFirst; // Распознанный первый кадр.
    public Frame frameLast; // Распознанный последний кадр.
    //
    public boolean isSelected; // Для интерактивного выбора - выбран для обработки.
    // При обработке:
    public long p_ParsedCount; // Кол-во распарсеных кадров.
    public long p_Count; // Кол-во обработанных кадров.
    public long p_Size; // Размер обработанных данных (без заголовков).
    public Frame p_First; // Обработанный первый кадр.
    public Frame p_Last; // Обработанный последний кадр.
    public long p_MinTime, p_MaxTime; // Минимальное и максимальное время среди обработанных кадров.
    public ArrayList<Skip> p_Skip; // Пропуски кадров (для оценки регистратора).
    public ArrayList<Error> p_Error; // Пропуски из-за нарушения файла.
    
    public class Skip {
        long time; // Время пропущенного кадра.
        long count; // Кол-во пропущенных кадров.
    }
            
    public class Error {
        long pos; // Позиция возникновения ошибки.
        long count; // Кол-во пропущенных байт.
    }
            
    public FileInfo() {
        fileName = null;
        fileSize = 0;
        fileType = -1;
        camNumbers = new ArrayList<Integer>();
        frameFirst = null;
        frameLast = null;
        isSelected = false;
    }
    
    // Компаратор для сортировки списка файлов.
    private static final Comparator<FileInfo> comparator = new Comparator<FileInfo>() {

        @Override
        public int compare(FileInfo o1, FileInfo o2) {
            long t1 = o1.frameFirst.time.getTime();
            long t2 = o2.frameFirst.time.getTime();
            return t1 == t2 ? 0 : (t1 < t2 ? -1 : 1);
        }
    };

    public static Comparator<FileInfo> getComparator() {
        return comparator;
    }
}
