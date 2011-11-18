/*
 * Copyright (c) 2011, Aleksey Nikolaevich Dokshin. All right reserved.
 * Contacts: dant.it@gmail.com, dokshin@list.ru.
 */
package dvrextract.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.table.TableColumnExt;

/**
 * GUI компонент для отображения типового справочника.
 * @author Докшин Алексей Николаевич <dant.it@gmail.com>
 */
public class JDirectory extends JPanel {

    // GUI компонент - таблица.
    protected JXTable table;
    // GUI компонент - панель прокрутки.
    protected JScrollPane scroll;
    // Флаг состояния горизонтальнй прокрутки (вкл/выкл).
    protected Boolean scrolled;
    // Модель столбцов таблицы.
    protected TableColumnModel columnModel;
    // Модель данных таблицы.
    protected AbstractTableModel tableModel;
    // Сохраненный режим растягивания столбцов (для восстановления после
    // выключения скроллинга).
    protected int savedResizeMode;

    /**
     * Конструктор.
     */
    public JDirectory(AbstractTableModel m, TableColumnModel tm) {
        tableModel = m;
        columnModel = tm;
        tm.linkToData(m); // Обязательно для корректного сопоставления!!!

        scrolled = false;
        table = new JXTable(tableModel, columnModel);
        table.setColumnControlVisible(true);
        table.setHighlighters(HighlighterFactory.createAlternateStriping(
                new Color(255, 255, 255), new Color(245, 250, 255)));
        // Установка параметров таблицы.
        // Сохранение редактируемого значения при потере фокуса ячейкой.
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        // Отключение начала редактирования ячейки по любой символьной клавише.
        table.putClientProperty("JTable.autoStartsEdit", Boolean.FALSE);
        // Установка отлова двойного клика на ячейке.
        //table.setSortable(true);
        table.addMouseListener(new ExtMouseAdapter() {

            @Override
            protected void fireDoubleClick(MouseEvent e) {
                fireEdit(new ActionEvent(e.getSource(), e.getID(), "mouse-doubleclick"));
            }
        });
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    ListSelectionModel l = (ListSelectionModel) e.getSource();
                    // Номер текущей строки таблицы
                    fireSelect(e, l);
                }
            }
        });
        // Установка своего обработчика нажатия клавиши Enter.
        table.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "key-enter");
        table.getActionMap().put("key-enter", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int row = table.getSelectedRow();
                int col = table.getSelectedColumn();
                if (table.isCellEditable(row, col)) {
                    table.editCellAt(row, col);
                } else {
                    fireEdit(new ActionEvent(e.getSource(), e.getID(), "key-enter"));
                }
            }
        });
        // Добавление скролинга контекста таблицы.
        scroll = new JScrollPane(table);
        scroll.addComponentListener(new java.awt.event.ComponentAdapter() {

            @Override
            public void componentResized(java.awt.event.ComponentEvent evt) {
                scrollComponentResized(evt);
            }
        });
        setLayout(new BorderLayout());
        add(scroll, BorderLayout.CENTER);
    }

    /**
     * Метод вызываемый при двойном клике на ячейке или нажатии Enter.
     * При условии, что ячейка ридонли, иначе - редактирование.
     * @param e Событие.
     */
    protected void fireEdit(ActionEvent e) {
    }

    protected void fireSelect(ListSelectionEvent e, ListSelectionModel l) {
    }

    /**
     * Добавление столбца в модель столбцов таблицы.
     * @param colname Имя (идентификатор) столбца.
     * @param header Заголовок.
     * @param width Ширина.
     * @param minwidth Минимальная ширина.
     * @param maxwidth Максимальная ширина.
     * @return Столбец.
     */
    public TableColumnExt addColumn(String colname, String header,
            int width, int minwidth, int maxwidth) {
        return columnModel.add(colname, header, width, minwidth, maxwidth);
    }

    /**
     * Обработчик изменения размеров компонента.
     * Включает по необходимости горизонтальный скроллинг и выключает при
     * отсутсвии необходимости в нем.
     * @param evt Событие.
     */
    private void scrollComponentResized(java.awt.event.ComponentEvent evt) {
        JViewport vp = scroll.getViewport();
        // Если суммарная ширина всех колонок больше видимой ширины компонента,
        if (vp.getSize().width < columnModel.getTotalColumnWidth()) {
            // то если скроллинг еще не был включен
            if (scrolled == false) {
                // отмечаем, что включаем его
                scrolled = true;
                // устанавливаем предпочитаемые ширины столбцов = реальным ширинам
                for (int i = 0; i < columnModel.getColumnCount(); i++) {
                    columnModel.getColumn(i).setPreferredWidth(
                            columnModel.getColumn(i).getWidth());
                }
                // убираем автоподбор ширины столбцов
                savedResizeMode = table.getAutoResizeMode();
                table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            }
        } else {
            // если же компонент шире колонок и включён скроллинг
            if (scrolled == true) {
                // то отмечаем, что выключаем
                scrolled = false;
                // устанавливаем предпочитаемые ширины столбцов = сохраненным ширинам
                for (int i = 0; i < columnModel.getColumnCount(); i++) {
                    columnModel.getColumn(i).setPreferredWidth(
                            columnModel.getColumnSavedWidth(i));
                }
                // устанавливаем автоподбор ширин столбцов
                table.setAutoResizeMode(savedResizeMode);
            }
        }
    }

    public JXTable getTable() {
        return table;
    }

    public JScrollPane getScroll() {
        return scroll;
    }

    public TableColumnModel getColumnModel() {
        return columnModel;
    }

    public void setColumnModel(TableColumnModel columnModel) {
        this.columnModel = columnModel;
        this.table.setColumnModel(columnModel);
        columnModel.linkToData(tableModel);
    }

    public AbstractTableModel getTableModel() {
        return tableModel;
    }

    public void setTableModel(AbstractTableModel tableModel) {
        this.tableModel = tableModel;
        this.table.setModel(tableModel);
        columnModel.linkToData(tableModel);
    }

    public void setModels(AbstractTableModel tableModel, TableColumnModel columnModel) {
        this.tableModel = tableModel;
        this.table.setModel(tableModel);
        this.columnModel = columnModel;
        this.table.setColumnModel(columnModel);
        columnModel.linkToData(tableModel);
    }

    /**
     * Делает указанную строку таблицы (не модели!!!) видимой в видимом окне 
     * скролла (прокручивает таблицу если нужно).
     * TODO допилисть чтобы это был номер строки модели!
     * @param index Номер строки.
     */
    public void displayTableRow(int index) {
        if (index >= 0 && index < table.getRowCount()) {
            Rectangle r = table.getCellRect(index, 0, true);
            scroll.getViewport().scrollRectToVisible(r);
        }
    }
}
