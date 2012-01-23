/*
 * Copyright (c) 2011-2012, Aleksey Nikolaevich Dokshin. All right reserved.
 * Contacts: dant.it@gmail.com, dokshin@list.ru.
 */
package dvrextract;

import java.util.ArrayList;
import java.util.Date;
import javax.swing.table.AbstractTableModel;

/**
 * Модель табличных данных для списка файлов.
 *
 * Подразумевается, что первый столбец невидимый и используется для системных
 * нужд.
 *
 * @author Докшин Алексей Николаевич <dant.it@gmail.com>
 */
public class FileListModel extends AbstractTableModel {

    /**
     * Типы столбцов.
     */
    private static Class[] colTypes = {FileInfo.class, String.class, String.class, Long.class, Date.class, Date.class};
    /**
     * Имена столбцов.
     */
    private static String[] colNames = {"ID", "name", "type", "size", "start", "end"};
    /**
     * Строки.
     */
    private final ArrayList<FileInfo> files;

    /**
     * Конструктор.
     *
     * @param cam Номер камеры для которой создаётся модель.
     */
    public FileListModel(int cam) {
        if (cam > 0 && cam <= App.MAXCAMS) {
            files = App.Source.getCamInfo(cam).files;
        } else {
            files = new ArrayList<FileInfo>();
        }
    }

    /**
     * Возвращает количество строк.
     *
     * @return Количество строк.
     */
    @Override
    public int getRowCount() {
        synchronized (files) {
            return files.size();
        }
    }

    /**
     * Возвращает количество столбцов.
     *
     * @return Количество столбцов.
     */
    @Override
    public int getColumnCount() {
        return colTypes.length; // имя,тип,размер,датамин,датамакс
    }

    /**
     * Возвращает тип данных столбца.
     *
     * @param column Номер столбца.
     * @return Тип данных столбца.
     */
    @Override
    public Class getColumnClass(int column) {
        return (column >= 0 && column < colTypes.length) ? colTypes[column] : String.class;
    }

    /**
     * Название столбца.
     *
     * @param column Столбец.
     * @return Название.
     */
    @Override
    public String getColumnName(int column) {
        return (column >= 0 && column < colNames.length) ? colNames[column] : "";
    }

    /**
     * Данные в ячейке.
     *
     * @param row Строка.
     * @param column Столбец.
     * @return Значение.
     */
    @Override
    public Object getValueAt(int row, int column) {
        synchronized (files) {
            switch (column) {
                case 0:
                    return files.get(row); // Невидимый!
                case 1:
                    return files.get(row).fileName.toString();
                case 2:
                    return files.get(row).fileType.title;
                case 3:
                    return files.get(row).fileSize;
                case 4:
                    return files.get(row).frameFirst.time;
                case 5:
                    return files.get(row).frameLast.time;
                default:
                    return "";
            }
        }
    }

    /**
     * Установка значения в ячейку.
     *
     * @param value Значение.
     * @param row Строка.
     * @param column Столбец.
     */
    @Override
    public void setValueAt(Object value, int row, int column) {
    }

    /**
     * Статус редактируемости для ячейки.
     *
     * @param rowIndex Строка.
     * @param columnIndex Столбец.
     * @return Статус (у нас всегда false - нельзя).
     */
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
}
