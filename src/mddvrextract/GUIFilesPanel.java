/*
 * Copyright (c) 2011-2013, Aleksey Nikolaevich Dokshin. All right reserved.
 * Contacts: dant.it@gmail.com, dokshin@list.ru.
 */
package mddvrextract;

import mddvrextract.gui.JDirectory;
import mddvrextract.gui.TableColumnModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.table.TableColumnExt;

/**
 * Панель со списком файлов-источников.
 * 
 * TODO: Реализовать выбор файлов? 
 * TODo: Двойной клик или выпад.меню для действий - выбор времени по файлу/группе файлов.
 *
 * @author Докшин Алексей Николаевич <dant.it@gmail.com>
 */
public final class GUIFilesPanel extends JPanel {

    /**
     * Панель - каталог.
     */
    private JFilesDirectory dir;
    /**
     * Номер камеры отображаемых файлов.
     */
    private int camNumber;
    /**
     * ID полей.
     */
    private static String ID_TYPE, ID_START, ID_END;
    /**
     * Форматтер представления дат.
     */
    private static DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    /**
     * Рендер ячеек.
     */
    private static TableCellRenderer cr = new FilesTableCellRenderer();
    /**
     * Связь на панель отображения инфы выбранного файла.
     */
    private GUIFileInfoPanel infoPanel;
    /**
     * Текстовые ресурсы для интерфейса.
     */
    public static String x_End, x_Name, x_Size, x_Start, x_Type;

    /**
     * Конструктор.
     *
     * @param info Ссылка на связанную панель вывода информации о файле (для
     * отображении инфы о файле при выборе файла).
     */
    public GUIFilesPanel(GUIFileInfoPanel info) {
        camNumber = 0;
        infoPanel = info;
        init();
    }

    /**
     * Инициализация графических компонентов. Построение модели для текущей
     * камеры (если camNumber=0 - пустой список).
     */
    private void init() {
        setLayout(new BorderLayout());

        FileListModel m = new FileListModel(camNumber);

        TableColumnModel cm = new TableColumnModel();
        TableColumnExt c;
        c = cm.add(m.getColumnName(0), "", 0, 0, 0);
        c.setVisible(false);
        cm.add(m.getColumnName(1), x_Name, -1, 300, -1);
        c = cm.add(ID_TYPE = m.getColumnName(2), x_Type, 100, 100, 100);
        c.setCellRenderer(cr);
        cm.add(m.getColumnName(3), x_Size, 120, 120, 120);
        c = cm.add(ID_START = m.getColumnName(4), x_Start, 150, 150, 150);
        c.setCellRenderer(cr);
        c = cm.add(ID_END = m.getColumnName(5), x_End, 150, 150, 150);
        c.setCellRenderer(cr);

        dir = new JFilesDirectory(m, cm);
        dir.getTable().setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
        add(dir, BorderLayout.CENTER);
    }

    /**
     * Актуализация контента при смене языка отображения.
     */
    public void updateLocale() {
        TableColumnModel cm = dir.getColumnModel();
        cm.setColumnHeader(1, x_Name);
        cm.setColumnHeader(2, x_Type);
        cm.setColumnHeader(3, x_Size);
        cm.setColumnHeader(4, x_Start);
        cm.setColumnHeader(5, x_End);
    }

    /**
     * Построение модели для указанного номера камеры (если номер камеры
     * соответствует текущему - построения не происходит).
     *
     * @param cam Номер камеры.
     */
    public void setModel(int cam) {
        if (cam != camNumber) {
            camNumber = cam;
            dir.setTableModel(new FileListModel(camNumber));
        }
    }

    /**
     * Обработка выбора файла в таблице файлов.
     *
     * @param row Строка (0-N - строка, -1-потеря выбора).
     * @param info Инфо о файле (при потере выбора = null).
     */
    public void fireSelect(int row, FileInfo info) {
        infoPanel.displayInfo(info, true);
    }

    /**
     * Реализация директории для справочника файлов.
     */
    private class JFilesDirectory extends JDirectory {

        /**
         * Конструктор.
         *
         * @param m Модель данных.
         * @param tm Модель столбцов.
         */
        public JFilesDirectory(AbstractTableModel m, TableColumnModel tm) {
            super(m, tm);
        }

        @Override
        protected void fireSelect(ListSelectionEvent e, ListSelectionModel l) {
            int n = table.getSelectedRow();
            if (n >= 0) {
                JXTable tab = dir.getTable();
                int indexId = tab.getColumnModel().getColumnIndex("ID");
                FileInfo info = (FileInfo) tab.getValueAt(tab.getSelectedRow(), indexId);
                GUIFilesPanel.this.fireSelect(n, info);
            } else {
                GUIFilesPanel.this.fireSelect(n, null);
            }
        }
    }

    /**
     * Реализация рендера ячеек таблицы для справочника файлов.
     */
    private static class FilesTableCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component com = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
            if (com instanceof DefaultTableCellRenderer) {
                DefaultTableCellRenderer r = (DefaultTableCellRenderer) com;
                Object id = table.getColumnModel().getColumn(column).getIdentifier();
                if (id == ID_TYPE) {
                    r.setHorizontalAlignment(JLabel.CENTER);
                } else if (id == ID_START || id == ID_END) {
                    r.setHorizontalAlignment(JLabel.CENTER);
                    if (value != null) {
                        r.setText(df.format((Date) value));
                    }
                }
            }
            return com;
        }
    }
}
