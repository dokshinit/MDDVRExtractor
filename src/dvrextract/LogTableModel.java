package dvrextract;

import java.util.*;
import javax.swing.table.AbstractTableModel;

/**
 * Модель табличных данных адаптированная для работы с БД
 *
 * @author Докшин_А_Н
 */
public class LogTableModel extends AbstractTableModel {

    public static enum Type {

        // Для маркировки строк с ошибками.
        ERROR,
        // Для вывода без подсветки (обычный текст).
        TEXT,
        // Для вывода неактуальной информации слабо подсвеченный текст.
        HIDE,
        // Для маркировки строк к предупреждениями или важной информацией.
        INFO
    };

    public static class Row {

        public Type type;
        public Date dt;
        public String text;

        public Row(Type type, Date dt, String text) {
            this.type = type;
            this.dt = dt;
            this.text = text;
        }
    }
    // Типы столбцов.
    private static Class[] colTypes = {Row.class, Date.class, String.class};
    // Имена столбцов.
    private static String[] colNames = {"ID", "dt", "text"};
    // Строки.
    private ArrayList<Row> data;

    /**
     * Конструктор.
     */
    public LogTableModel() {
        super();
        data = new ArrayList<Row>();
    }

    /**
     * Возвращает количество строк.
     * @return Количество строк.
     */
    @Override
    public int getRowCount() {
        synchronized (data) {
            return data.size();
        }
    }

    /**
     * Возвращает количество столбцов.
     * @return Количество столбцов.
     */
    @Override
    public int getColumnCount() {
        return colTypes.length; // имя,тип,размер,датамин,датамакс
    }

    /**
     * Возвращает тип данных столбца.
     * @param column Номер столбца.
     * @return Тип данных столбца.
     */
    @Override
    public Class getColumnClass(int column) {
        return (column >= 0 && column < colTypes.length) ? colTypes[column] : String.class;
    }

    /**
     * Название столбца.
     * @param column Столбец.
     * @return Название.
     */
    @Override
    public String getColumnName(int column) {
        return (column >= 0 && column < colNames.length) ? colNames[column] : "";
    }
    
    /**
     * Данные в ячейке.
     * @param row Строка.
     * @param column Столбец.
     * @return Значение.
     */
    @Override
    public Object getValueAt(int row, int column) {
        synchronized (data) {
            switch (column) {
                case 0:
                    return data.get(row); // Невидимый!
                case 1:
                    return data.get(row).dt;
                case 2:
                    return data.get(row).text;
            }
            return "";
        }
    }

    /**
     * Установка значения в ячейку.
     * @param value Значение.
     * @param row Строка.
     * @param column Столбец.
     */
    @Override
    public void setValueAt(Object value, int row, int column) {
    }

    /**
     * Статус редактируемости для ячейки.
     * @param rowIndex Строка.
     * @param columnIndex Столбец.
     * @return Статус (у нас всегда false - нельзя).
     */
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
    
    public void add(Type type, Date dt, String text) {
        synchronized (data) {
            data.add(new Row(type, dt, text));
            fireTableRowsInserted(data.size() - 1, data.size() - 1);
        }
    }

    public void add(Type type, String text) {
        synchronized (data) {
            data.add(new Row(type, new Date(), text));
            fireTableRowsInserted(data.size() - 1, data.size() - 1);
        }
    }

    /**
     * Обновляется последняя строка лога - добавляется текст и устанавливается
     * тип. Время не изменяется!
     * @param type Новый тип.
     * @param text Добавляемый к концу строки текст.
     */
    public void update(Type type, String text) {
        synchronized (data) {
            int i = data.size() - 1;
            if (i >= 0) {
                Row row = data.get(i);
                row.type = type;
                row.text += text;
                data.set(i, row);
                fireTableRowsUpdated(i, i);
            } else {
                add(type, text);
            }
        }
    }

    public void removeAll() {
        synchronized (data) {
            int size = data.size();
            if (size > 0) {
                data.clear();
                fireTableRowsDeleted(0, size - 1);
            }
        }
    }
}