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
public class CamInfo {

    public boolean isExists; // для архива - если есть флаг, для hdd - если есть файлы
    public ArrayList<FileInfo> files;
    // При обработке:
    public long p_ParsedCount; // Кол-во распарсеных кадров.
    public long p_Count; // Кол-во обработанных кадров.
    public long p_Size; // Размер обработанных данных.
    public long p_RawSize; // Размер обработанных данных всего?
    public Frame p_First; // Обработанный первый кадр.
    public Frame p_Last; // Обработанный последний кадр.
    public long p_MinTime, p_MaxTime; // Минимальное и максимальное время среди обработанных кадров.

    public CamInfo() {
        isExists = false;
        files = new ArrayList<FileInfo>();
    }

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
    
    public void addFile(FileInfo info) {
        files.add(info);
    }
}
