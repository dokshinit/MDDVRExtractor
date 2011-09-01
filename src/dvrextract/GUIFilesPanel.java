package dvrextract;

import dvrextract.gui.FormattedDateValue;
import dvrextract.gui.JDirectory;
import dvrextract.gui.TableColumnModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
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
    TableCellRenderer cr = new DefaultTableCellRenderer() {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component com = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
            if (com instanceof DefaultTableCellRenderer) {
                DefaultTableCellRenderer r = (DefaultTableCellRenderer) com;
                if (column == 2) {
                    r.setHorizontalAlignment(JLabel.CENTER);
                }
            }
            return com;
        }
    };

    /**
     * Инициализация списка для указанной камеры (если cam=0 - пустой список).
     * @param cam Номер камеры или ноль.
     */
    public void init() {
        setLayout(new BorderLayout());

        TableColumnModel tm = new TableColumnModel();
        TableColumnExt c;
        c = tm.add("x", "x", 20, 20, 20);
        c = tm.add("name", "Имя файла", -1, 200, -1);
        c = tm.add("type", "Тип", 100, 100, 100);
        c.setCellRenderer(cr);
        tm.add("size", "Размер", 100, 100, 150);
        c = tm.add("start", "Начало", 150, 150, 150);
        c.setCellRenderer(timeRender);
        c = tm.add("end", "Конец", 150, 150, 150);
        c.setCellRenderer(timeRender);

        dir = new JDirectory(new FileListModel(camNumber), tm) {

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
