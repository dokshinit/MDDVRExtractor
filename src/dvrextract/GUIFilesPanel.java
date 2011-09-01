package dvrextract;

import java.awt.BorderLayout;
import java.util.Date;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

/**
 * Графический список файлов.
 * @author lex
 */
public final class GUIFilesPanel extends JPanel {

    FileListModel model;
    JScrollPane scroll;
    JTable list;
    int camNumber;
    
    public GUIFilesPanel() {
        camNumber = 0;
        init();
    }
    
    /**
     * Инициализация списка для указанной камеры (если cam=0 - пустой список).
     * @param cam Номер камеры или ноль.
     */
    public void init() {
        setLayout(new BorderLayout());
        
        model = new FileListModel(camNumber);
        list = new JTable(model);
        scroll = new JScrollPane(list);
        add(scroll, BorderLayout.CENTER);
    }
    
    public void selectCamModel(int cam) {
        if (cam != camNumber) {
            camNumber = cam;
            model = new FileListModel(cam);
            list.setModel(model);
        }
    }
    
    public void test() {
        App.srcCams[0] = new CamInfo();
        for (int i=0; i<10; i++) {
            FileInfo info = new FileInfo();
            info.fileName = "file"+i;
            info.fileType = FileType.EXE;
            info.fileSize = 10000;
            info.frameFirst = new Frame(info.fileType);
            info.frameFirst.time = new Date();
            info.frameLast = new Frame(info.fileType);
            info.frameLast.time = new Date();
            App.srcCams[0].files.add(info);
        }
        selectCamModel(1);
    }
}
