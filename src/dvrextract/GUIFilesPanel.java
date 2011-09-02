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
    static DefaultTableRenderer timeRender = new DefaultTableRenderer(
            new FormattedDateValue("dd.MM.yyyy HH:mm:ss"));
    static DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
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

        TableColumnModel tm = new TableColumnModel();
        TableColumnExt c;
        c = tm.add(m.getColumnName(0), "x", 20, 20, 20);
        c = tm.add(m.getColumnName(1), "Имя", -1, 300, -1);
        c = tm.add(ID_TYPE = m.getColumnName(2), "Тип", 100, 100, 100);
        c.setCellRenderer(cr);
        tm.add(m.getColumnName(3), "Размер", 120, 120, 120);
        c = tm.add(ID_START = m.getColumnName(4), "Начало", 150, 150, 150);
        c.setCellRenderer(cr);
        //c.setCellRenderer(timeRender);
        c = tm.add(ID_END = m.getColumnName(5), "Конец", 150, 150, 150);
        //c.setCellRenderer(timeRender);
        c.setCellRenderer(cr);

        dir = new JDirectory(m, tm) {

            @Override
            protected void fireEdit(ActionEvent e) {
                App.log("Edit!");
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
