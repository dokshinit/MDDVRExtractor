/*
 * Copyright (c) 2011-2013, Aleksey Nikolaevich Dokshin. All right reserved.
 * Contacts: dant.it@gmail.com, dokshin@list.ru.
 */
package mddvrextract.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import mddvrextract.Resources;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.table.TableColumnExt;

/**
 * GUI компонент для отображения типового справочника.
 *
 * @author Докшин Алексей Николаевич <dant.it@gmail.com>
 */
public class JDirectory extends JPanel {

    /**
     * GUI компонент - таблица.
     */
    protected JXTable table;
    /**
     * GUI компонент - панель прокрутки.
     */
    protected JScrollPane scroll;
    /**
     * Флаг состояния горизонтальнй прокрутки (вкл/выкл).
     */
    protected Boolean scrolled;
    /**
     * Модель столбцов таблицы.
     */
    protected TableColumnModel columnModel;
    /**
     * Модель данных таблицы.
     */
    protected AbstractTableModel tableModel;
    /**
     * Сохраненный режим растягивания столбцов (для восстановления после
     * выключения скроллинга).
     */
    protected int savedResizeMode;
    /**
     * Отрисовка заголовка таблицы.
     */
    private static ExtHeaderRenderer headerRenderer = new ExtHeaderRenderer();

    /**
     * Конструктор.
     *
     * @param model Модель данных таблицы.
     * @param cmodel Модель столбцов таблицы.
     */
    public JDirectory(AbstractTableModel model, TableColumnModel cmodel) {
        tableModel = model;
        columnModel = cmodel;
        cmodel.linkToData(model); // Обязательно для корректного сопоставления!!!

        scrolled = false;
        table = new JXTable(tableModel, columnModel);
        table.setColumnControlVisible(false);

        Color tabColor = UIManager.getColor("Table.background");
        Color rowColor1 = tabColor;
        Color rowColor2 = new Color((int) (tabColor.getRed() * .98),
                (int) (tabColor.getGreen() * .95),
                (int) (tabColor.getBlue() * .95));
        table.setRowHeight(table.getRowHeight() + 4);

        for (int i = 0; i < cmodel.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }

        table.setHighlighters(HighlighterFactory.createAlternateStriping(
                rowColor1, rowColor2));
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
        //scroll.setBorder(new LineBorder(GUI.c_Base, 1));

        //scroll.getViewport().setBackground(rowColors[1]);
        //table.setBackground(rowColor2);
        setLayout(new BorderLayout());
        add(scroll, BorderLayout.CENTER);
    }

    /**
     * Метод вызываемый при двойном клике на ячейке или нажатии Enter. При
     * условии, что ячейка ридонли, иначе - редактирование.
     *
     * @param e Событие.
     */
    protected void fireEdit(ActionEvent e) {
    }

    /**
     * Метод вызываемый при выборе строки в таблице.
     *
     * @param e Событие.
     * @param l Модель выделения.
     */
    protected void fireSelect(ListSelectionEvent e, ListSelectionModel l) {
    }

    /**
     * Добавление столбца в модель столбцов таблицы.
     *
     * @param colname Имя (идентификатор) столбца.
     * @param header Заголовок.
     * @param width Ширина.
     * @param minwidth Минимальная ширина.
     * @param maxwidth Максимальная ширина.
     * @return Столбец.
     */
    public TableColumnExt addColumn(String colname, String header,
            int width, int minwidth, int maxwidth) {
        TableColumnExt col = columnModel.add(colname, header, width, minwidth, maxwidth);
        col.setHeaderRenderer(headerRenderer);
        return col;
    }

    /**
     * Обработчик изменения размеров компонента. Включает по необходимости
     * горизонтальный скроллинг и выключает при отсутсвии необходимости в нем.
     *
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

    /**
     * Возвращает компонент таблицы.
     *
     * @return Таблица.
     */
    public JXTable getTable() {
        return table;
    }

    /**
     * Возвращает скролл компонент.
     *
     * @return Скролл.
     */
    public JScrollPane getScroll() {
        return scroll;
    }

    /**
     * Возвращает модель столбцов таблицы.
     *
     * @return Модель столбцов таблицы.
     */
    public TableColumnModel getColumnModel() {
        return columnModel;
    }

    /**
     * Установка новой модели столбцов таблицы.
     *
     * @param columnModel Модель столбцов таблицы.
     */
    public void setColumnModel(TableColumnModel columnModel) {
        this.columnModel = columnModel;
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }
        this.table.setColumnModel(columnModel);
        columnModel.linkToData(tableModel);
    }

    /**
     * Возвращает модель таблицы.
     *
     * @return Модель таблицы.
     */
    public AbstractTableModel getTableModel() {
        return tableModel;
    }

    /**
     * Установка новой модели данных таблицы.
     *
     * @param tableModel Модель данных таблицы.
     */
    public void setTableModel(AbstractTableModel tableModel) {
        this.tableModel = tableModel;
        this.table.setModel(tableModel);
        columnModel.linkToData(tableModel);
    }

    /**
     * Установка моделей таблицы.
     *
     * @param tableModel Модель данных.
     * @param columnModel Модель столбцов.
     */
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
     *
     * @param index Номер строки.
     */
    public void displayTableRow(int index) {
        if (index >= 0 && index < table.getRowCount()) {
            Rectangle r = table.getCellRect(index, 0, true);
            scroll.getViewport().scrollRectToVisible(r);
        }
    }
}

/**
 * Рендер для отрисовки заголовков таблицы.
 *
 * @author Докшин Алексей Николаевич <dant.it@gmail.com>
 */
class ExtHeaderRenderer extends DefaultTableCellRenderer {

    private Icon sortArrow = null;

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        setText(value == null ? "" : value.toString());
        setForeground(Color.WHITE);
        setBackground(GUI.c_Base);
        setBorder(new ExtTableHeaderBorder(GUI.c_BaseLight, GUI.c_BaseDark));
        setHorizontalAlignment(JLabel.CENTER);
        sortArrow = null;

        if (table != null && table.getRowSorter() != null) {
            List<? extends RowSorter.SortKey> sortKeys = table.getRowSorter().getSortKeys();
            if (sortKeys.size() > 0
                    && sortKeys.get(0).getColumn() == table.convertColumnIndexToModel(column)) {
                switch (sortKeys.get(0).getSortOrder()) {
                    case ASCENDING:
                        sortArrow = Resources.GUI.imageDownArrow;
                        break;
                    case DESCENDING:
                        sortArrow = Resources.GUI.imageUpArrow;
                        break;
                }
            }
        }
        return this;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (sortArrow != null) {
            int x = getWidth() - sortArrow.getIconWidth() - 6;
            int y = (getHeight() - sortArrow.getIconHeight()) / 2;
            sortArrow.paintIcon(this, g, x, y);
        }
    }
}

/**
 * Окантовка заголовков таблицы.
 *
 * @author Докшин Алексей Николаевич <dant.it@gmail.com>
 */
class ExtTableHeaderBorder implements Border {

    private Color light, dark;

    public ExtTableHeaderBorder(Color light, Color dark) {
        this.light = light;
        this.dark = dark;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(light);
        g2.drawLine(x, y, x + width - 1, y);
        g2.drawLine(x, y, x, y + height - 1);
        g2.setColor(dark);
        g2.drawLine(x, y + height - 1, x + width - 1, y + height - 1);
        g2.drawLine(x + width - 1, y, x + width - 1, y + height - 1);
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(2, 6, 2, 6);
    }

    @Override
    public boolean isBorderOpaque() {
        return false;
    }
}