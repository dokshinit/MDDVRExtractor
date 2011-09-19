package dvrextract;

import dvrextract.gui.JDirectory;
import dvrextract.gui.TableColumnModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.table.TableColumnExt;

/**
 * Панель со списком файлов-источников.
 * @author lex
 */
public final class GUIFilesPanel extends JPanel {

    JDirectory dir;
    // Номер камеры отображаемых файлов.
    int camNumber;

    public GUIFilesPanel() {
        camNumber = 0;
        init();
    }
    // Форматтер представления дат.
    static DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    // Рендер ячеек.
    static TableCellRenderer cr = new DefaultTableCellRenderer() {

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
    };
    static String ID_TYPE, ID_START, ID_END;

    /**
     * Инициализация списка для указанной камеры (если cam=0 - пустой список).
     * @param cam Номер камеры или ноль.
     */
    public void init() {
        setLayout(new BorderLayout());

        FileListModel m = new FileListModel(camNumber);

        TableColumnModel cm = new TableColumnModel();
        TableColumnExt c;
        c = cm.add(m.getColumnName(0), "", 0, 0, 0);
        c.setVisible(false);
        cm.add(m.getColumnName(1), "Имя", -1, 300, -1);
        c = cm.add(ID_TYPE = m.getColumnName(2), "Тип", 100, 100, 100);
        c.setCellRenderer(cr);
        cm.add(m.getColumnName(3), "Размер", 120, 120, 120);
        c = cm.add(ID_START = m.getColumnName(4), "Начало", 150, 150, 150);
        c.setCellRenderer(cr);
        c = cm.add(ID_END = m.getColumnName(5), "Конец", 150, 150, 150);
        c.setCellRenderer(cr);

        dir = new JDirectory(m, cm) {

            @Override
            protected void fireEdit(ActionEvent e) {
                //App.log("Edit!");
            }

            @Override
            protected void fireSelect(ListSelectionEvent e, ListSelectionModel l) {
                int n = table.getSelectedRow();
                if (n >= 0) {
                    JXTable table = dir.getTable();
                    int indexId = table.getColumnModel().getColumnIndex("ID");
                    FileInfo info = (FileInfo) table.getValueAt(table.getSelectedRow(), indexId);
                    fireFileSelect(n, info);
                } else {
                    fireFileSelect(n, null);
                }
            }
        };

        add(dir, BorderLayout.CENTER);
    }

    public void selectCamModel(int cam) {
        if (cam != camNumber) {
            camNumber = cam;
            dir.setTableModel(new FileListModel(camNumber));
        }
    }

    public void fireFileSelect(int row, FileInfo info) {
        //App.log("Select! row=" + n);
        //App.log("N=" + n + " name=" + info.fileName);
        //App.mainFrame.tabSource.infoPanel.displayInfo(info);
        //App.mainFrame.tabSource.infoPanel.displayInfo(null);
    }
}
