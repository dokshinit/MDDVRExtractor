/*
 * Copyright (c) 2011, Aleksey Nikolaevich Dokshin. All right reserved.
 * Contacts: dant.it@gmail.com, dokshin@list.ru.
 */
package dvrextract;

import dvrextract.gui.JDirectory;
import dvrextract.gui.TableColumnModel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import net.miginfocom.swing.MigLayout;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.table.TableColumnExt;

/**
 * Панель для вывода логов.
 * TODO: Реализовать просмотр/копирование содержимого ячейки в буфер (?).
 * @author Докшин Алексей Николаевич <dant.it@gmail.com>
 */
public class GUILogPanel extends JPanel {

    /**
     * Панель - лог.
     */
    private JDirectory dir;
    /**
     * Модель лога.
     */
    private LogTableModel model;
    /**
     * ID полей.
     */
    private static String ID_DT, ID_TEXT;
    /**
     * Форматтер представления дат.
     */
    private static DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    /**
     * Рендер ячеек.
     */
    private static TableCellRenderer cellRender = new LogTableCellRenderer();
    /**
     * Текстовые ресурсы для интерфейса.
     */
    public static String x_Date, x_Message;

    /**
     * Конструктор.
     */
    public GUILogPanel() {
        super();
        init();
    }

    /**
     * Инициализация графических компонентов.
     */
    protected void init() {
        setLayout(new MigLayout("fill"));

        model = new LogTableModel();
        TableColumnModel cm = new TableColumnModel();
        TableColumnExt c;
        c = cm.add(model.getColumnName(0), "", 0, 0, 0);
        c.setVisible(false);
        c = cm.add(ID_DT = model.getColumnName(1), x_Date, 150, 150, 150);
        c.setCellRenderer(cellRender);
        c = cm.add(ID_TEXT = model.getColumnName(2), x_Message, -1, 150, -1);
        c.setCellRenderer(cellRender);

        dir = new JDirectory(model, cm);
        JXTable table = dir.getTable();
        table.setSortable(false);
        table.addHighlighter(new LogHighlighter());
        table.setGridColor(new Color(0xD0D0D0));

        add(dir, "grow");
        setPreferredSize(new Dimension(800, 400));
    }

    /**
     * Актуализация контента при смене языка отображения.
     */
    public void updateLocale() {
        TableColumnModel cm = dir.getColumnModel();
        cm.setColumnHeader(1, x_Date);
        cm.setColumnHeader(2, x_Message);
    }

    /**
     * Добавление строки лога.
     * @param type Тип сообщения.
     * @param text Текст сообщения.
     */
    public void add(LogTableModel.Type type, String text) {
        model.add(type, text);
        // Позиционируем в конец лога.
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                dir.displayTableRow(model.getRowCount() - 1);
            }
        });

    }

    /**
     * Обновление последней строки лога. Если лог пуст - добавление строки.
     * @param type Тип сообщения.
     * @param text Текст сообщения.
     */
    public void update(LogTableModel.Type type, String text) {
        model.update(type, text);
    }

    /**
     * Очистка лога.
     */
    public void clear() {
        model.removeAll();
    }

    /**
     * Реализация рендера ячеек таблицы для лога.
     */
    private static class LogTableCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component com = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
            if (com instanceof DefaultTableCellRenderer) {
                DefaultTableCellRenderer r = (DefaultTableCellRenderer) com;
                Object id = table.getColumnModel().getColumn(column).getIdentifier();
                if (id == ID_DT) {
                    r.setHorizontalAlignment(JLabel.CENTER);
                    if (value != null) {
                        r.setText(dateFormat.format((Date) value));
                    }
                } else if (id == ID_TEXT) {
                    r.setHorizontalAlignment(JLabel.LEFT);
                    //setToolTipText((String)value);
                }
            }
            return com;
        }
    }
}
