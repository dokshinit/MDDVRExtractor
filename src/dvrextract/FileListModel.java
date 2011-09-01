package dvrextract;

import java.util.ArrayList;
import java.util.Date;
import javax.swing.table.AbstractTableModel;

/**
 * Модель табличных данных для списка файлов.
 *
 * @author Докшин_А_Н
 */
public class FileListModel extends AbstractTableModel {

    private final ArrayList<FileInfo> files;

    /**
     * Конструктор.
     */
    public FileListModel(int cam) {
        if (cam > 0 && cam <= App.MAXCAMS) {
            files = App.srcCams[cam - 1].files;
        } else {
            files = new ArrayList<FileInfo>();
        }
    }

    /**
     * Возвращает количество строк.
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
     * @return Количество столбцов.
     */
    @Override
    public int getColumnCount() {
        return 6; // выбор,имя,тип,размер,датамин,датамакс
    }

    /**
     * Возвращает тип данных столбца.
     * @param column Номер столбца.
     * @return Тип данных столбца.
     */
    @Override
    public Class getColumnClass(int column) {
        switch (column) {
            case 0:
                return Boolean.class;
            case 1:
                return String.class;
            case 2:
                return String.class;
            case 3:
                return Long.class;
            case 4:
                return Date.class;
            case 5:
                return Date.class;
            default:
                return String.class;
        }
    }

    // название столбца
    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0:
                return "x";
            case 1:
                return "Имя файла";
            case 2:
                return "Тип";
            case 3:
                return "Размер";
            case 4:
                return "Начало";
            case 5:
                return "Конец";
            default:
                return "";
        }
    }

    // данные в ячейке
    @Override
    public Object getValueAt(int row, int column) {
        synchronized (files) {
            switch (column) {
                case 0:
                    return files.get(row).isSelected;
                case 1:
                    return files.get(row).fileName;
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

    // замена значения ячейки
    @Override
    public void setValueAt(Object value, int row, int column) {
        synchronized (files) {
            if (column == 0) {
                files.get(row).isSelected = (Boolean) value;
            }
        }
    }

    //    fireTableDataChanged();
}
