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
    // Типы столбцов.
    static Class[] colTypes = {Boolean.class, String.class, String.class, Long.class, Date.class, Date.class};
    // Имена столбцов.
    static String[] colNames = {"x", "name", "type", "size", "start", "end"};

    /**
     * Возвращает тип данных столбца.
     * @param column Номер столбца.
     * @return Тип данных столбца.
     */
    @Override
    public Class getColumnClass(int column) {
        return (column >= 0 && column < colTypes.length) ? colTypes[column] : String.class;
    }

    // название столбца
    @Override
    public String getColumnName(int column) {
        return (column >= 0 && column < colNames.length) ? colNames[column] : "";
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

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 0 ? true : false;
    }
    
    
}