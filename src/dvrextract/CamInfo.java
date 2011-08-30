/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dvrextract;

import java.util.ArrayList;
import java.util.Date;

/**
 *
 * @author lex
 */
public final class CamInfo {

    ////////////////////////////////////////////////////////////////////////////
    // Информация о файле при сканировании.
    ////////////////////////////////////////////////////////////////////////////
    // Флаг наличия данных по этой камере.
    public boolean isExists;
    // Файлы содержащие данные этой камеры.
    public ArrayList<FileInfo> files;
    
    ////////////////////////////////////////////////////////////////////////////
    // Информация заполняемая при обработке. Нужна ли? Может рассчитывать из файлов?
    ////////////////////////////////////////////////////////////////////////////
    // Кол-во распарсеных кадров.
    public long p_ParsedCount; 
    // Кол-во обработанных кадров.
    public long p_Count;
    // Размер обработанных данных.
    public long p_Size; 
    // Размер обработанных данных всего?
    public long p_RawSize;
    // Обработанный первый кадр.
    public Frame p_First;
    // Обработанный последний кадр.
    public Frame p_Last;
    // Минимальное и максимальное время среди обработанных кадров.
    public long p_MinTime, p_MaxTime; 

    /**
     * Конструктор.
     */
    public CamInfo() {
        isExists = false;
        files = new ArrayList<FileInfo>();
        clear();
    }

    /**
     * Очистка информации.
     */
    public void clear() {
        isExists = false;
        files.clear();
        p_ParsedCount = 0;
        p_Count = 0;
        p_Size = 0;
        p_First = null;
        p_Last = null;
        p_MinTime = 0;
        p_MaxTime = 0;
    }
    
    /**
     * Добавление файла в список файлов камеры.
     * @param info 
     */
    public void addFile(FileInfo info) {
        files.add(info);
    }
}
