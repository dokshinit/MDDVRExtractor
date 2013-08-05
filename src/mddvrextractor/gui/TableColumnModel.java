/*
 * Copyright (c) 2011-2013, Aleksey Nikolaevich Dokshin. All right reserved.
 * Contacts: dant.it@gmail.com, dokshin@list.ru.
 */
package mddvrextractor.gui;

import java.util.ArrayList;
import java.util.Comparator;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import org.jdesktop.swingx.table.TableColumnExt;
//import org.jdesktop.swingx.table.TableColumnExt;

/**
 * Модель колонок таблицы
 *
 * @author Докшин Алексей Николаевич <dant.it@gmail.com>
 */
public class TableColumnModel extends DefaultTableColumnModel {

    /**
     * Массив в котором сохраняются предпочитаемые ширины колонок Используется в
     * механизме изменения размеров компонента (включение и выключение
     * скроллинга) для корректного растяжения столбцов после отключения
     * скроллинга
     */
    private ArrayList<Integer> columnSavedWidth;

    /**
     * Конструктор
     */
    public TableColumnModel() {
        columnSavedWidth = new ArrayList<Integer>();
    }

    /**
     * Возвращает сохраненный предпочитаемый размер для колонки
     *
     * @param index Индекс колонки
     * @return Предпочтительная ширина колонки
     */
    public int getColumnSavedWidth(int index) {
        return columnSavedWidth.get(index);
    }

    /**
     * Добавление в модель колонки с заданием ее параметров
     *
     * @param colname Наименование столбца в модели данных
     * @param header Текст заголовка столбца
     * @param width Ширина столбца предпочтительная (-1 - не задано)
     * @param minwidth Минимальная ширина (-1 - не задано)
     * @param maxwidth Максимальная ширина (-1 - не задано)
     * @return Добавленный столбец (для возможной дополнительной настройки)
     */
    public TableColumnExt add(String colname, String header,
            int width, int minwidth, int maxwidth) {
        TableColumnExt col = (TableColumnExt) new TableColumnExt();
        col.setIdentifier(colname);
        col.setHeaderValue(header);
        if (width != -1) {
            col.setPreferredWidth(width);
        }
        if (minwidth != -1) {
            col.setMinWidth(minwidth);
        }
        if (maxwidth != -1) {
            col.setMaxWidth(maxwidth);
        }

        addColumn(col);
        columnSavedWidth.add(width);
        return col;
    }

    /**
     * Сопоставление столбцов и данных (по идентификаторам), присваивоение
     * индексов.
     *
     * @param model Модель данных таблицы.
     */
    public void linkToData(AbstractTableModel model) {
        TableColumn col = null;
        for (int i = 0; i < tableColumns.size(); i++) {
            col = tableColumns.get(i);
            int j = model.findColumn((String) col.getIdentifier());
            if (j >= 0) {
                col.setModelIndex(j);
            }
        }
    }

    /**
     * Возвращает столбец соответствующий указанному номеру В МОДЕЛИ (!).
     *
     * @param index Номер столбца в модели (0..n-1).
     * @return Столбец или null если не найден.
     */
    public TableColumn getColumnForModelIndex(int index) {
        TableColumn col = null;
        for (int i = 0; i < tableColumns.size(); i++) {
            col = tableColumns.get(i);
            if (col.getModelIndex() == index) {
                return col;
            }
        }
        return null;
    }

    /**
     * Устанавливает отображаемое название столбцу соответствующему заданному
     * номеру столбца В МОДЕЛИ (!).
     *
     * @param index Номер столбца В МОДЕЛИ (!).
     * @param title Название столбца.
     * @return Результат операции: true - успех, false - ошибка.
     */
    public boolean setColumnHeader(int index, String title) {
        TableColumn col = getColumnForModelIndex(index);
        if (col != null) {
            col.setHeaderValue(title);
            return true;
        }
        return false;
    }

    /**
     * Удаление из модели всех колонок
     */
    public void clear() {
        for (int i = getColumnCount() - 1; i >= 0; i--) {
            removeColumn(getColumn(i));
        }
        columnSavedWidth.clear();
    }

    /**
     * Компаратор для сортировки столбцов со значениями типа Double.
     */
    public class DoubleComparator implements Comparator {

        @Override
        public int compare(Object o1, Object o2) {
            Double d1 = Double.valueOf(o1 == null ? "0" : (String) o1);
            Double d2 = Double.valueOf(o2 == null ? "0" : (String) o2);
            return d1.compareTo(d2);
        }
    };
}
