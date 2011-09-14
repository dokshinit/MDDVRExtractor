package dvrextract;

import dvrextract.gui.FormattedDateValue;
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
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.table.TableColumnExt;

/**
 * Графический список файлов.
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
        cm.add(m.getColumnName(0), "Имя", -1, 300, -1);
        c = cm.add(ID_TYPE = m.getColumnName(1), "Тип", 100, 100, 100);
        c.setCellRenderer(cr);
        cm.add(m.getColumnName(2), "Размер", 120, 120, 120);
        c = cm.add(ID_START = m.getColumnName(3), "Начало", 150, 150, 150);
        c.setCellRenderer(cr);
        c = cm.add(ID_END = m.getColumnName(4), "Конец", 150, 150, 150);
        c.setCellRenderer(cr);

        dir = new JDirectory(m, cm) {

            @Override
            protected void fireEdit(ActionEvent e) {
                App.log("Edit!");
            }

            @Override
            protected void fireSelect(ListSelectionEvent e, ListSelectionModel l) {
                App.log("Select! row="+l.getMinSelectionIndex());
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
}
