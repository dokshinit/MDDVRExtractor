/*
 * Copyright (c) 2011-2013, Aleksey Nikolaevich Dokshin. All right reserved.
 * Contacts: dant.it@gmail.com, dokshin@list.ru.
 */
package mddvrextract;

import java.util.ArrayList;

/**
 * Информация по камере (создаётся при сканировании, дополняется при обработке).
 *
 * @author Докшин Алексей Николаевич <dant.it@gmail.com>
 */
public final class CamInfo {

    ////////////////////////////////////////////////////////////////////////////
    // Информация получаемая при сканировании.
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Флаг наличия данных по этой камере.
     */
    public boolean isExists;
    /**
     * Файлы содержащие данные этой камеры.
     */
    public ArrayList<FileInfo> files;
    ////////////////////////////////////////////////////////////////////////////
    // Информация заполняемая при обработке.
    // Для отражении на вкладке "Состояние". Оставлены на будущее.
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Кол-во распарсеных кадров.
     */
    public long p_ParsedCount;
    /**
     * Кол-во обработанных кадров.
     */
    public long p_Count;
    /**
     * Размер обработанных данных.
     */
    public long p_Size;
    /**
     * Размер обработанных данных всего.
     */
    public long p_RawSize;
    /**
     * Обработанный первый кадр.
     */
    public Frame p_First;
    /**
     * Обработанный последний кадр.
     */
    public Frame p_Last;
    /**
     * Минимальное и максимальное время среди обработанных кадров.
     */
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
     *
     * @param info
     */
    public void addFile(FileInfo info) {
        isExists = true;
        files.add(info);
    }
}
