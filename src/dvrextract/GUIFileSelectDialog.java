package dvrextract;

import dvrextract.gui.GUIDialog;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

/**
 * Диалог выбора файла/каталога (для чтения/записи).
 * @author lex
 */
public class GUIFileSelectDialog extends GUIDialog {

    protected FileChooser fc;

    /**
     * Цель вызова диалога
     */
    public static enum Target {

        // Выбор существующего файла/каталога.
        EXIST_ONLY,
        // Создание нового файла/перезапись существующего.
        NEW_OR_EXIST
    }

    /**
     * Режим работы диалога.
     */
    public static enum Mode {

        DIR(JFileChooser.DIRECTORIES_ONLY),
        FILE(JFileChooser.FILES_ONLY),
        ALL(JFileChooser.FILES_AND_DIRECTORIES);
        // ID соответствующий режиму JFileChooser.
        public int id;

        Mode(int id) {
            this.id = id;
        }
    }
    // Цель диалога.
    private Target target;
    // Режим диалога.
    private Mode mode;

    protected GUIFileSelectDialog(String title, String fname, Target tgt, Mode m) {
        setTitle(title);

        fname = fname != null ? fname : "";
        target = tgt;
        mode = m;

        UIManager.put("FileChooser.readOnly", tgt == Target.EXIST_ONLY);
        UIManager.put("FileChooser.cancelButtonText", "Отмена");
        UIManager.put("FileChooser.cancelButtonToolTipText", "Отмена выбора");
        UIManager.put("FileChooser.detailsViewButtonToolTipText", "Детальный вид");
        UIManager.put("FileChooser.listViewButtonToolTipText", "В виде списка");
        UIManager.put("FileChooser.fileNameLabelText", "Имя файла:");
        UIManager.put("FileChooser.filesOfTypeLabelText", "Тип файлов:");
        UIManager.put("FileChooser.homeFolderToolTipText", "Домашний каталог");
        UIManager.put("FileChooser.lookInLabelText", "Каталог:");
        UIManager.put("FileChooser.openButtonText", "Выбрать");
        UIManager.put("FileChooser.openButtonToolTipText", "Выбрать файл");
        UIManager.put("FileChooser.upFolderToolTipText", "На уровень вверх");
        UIManager.put("FileChooser.fileDateHeaderText", "Дата/время");
        UIManager.put("FileChooser.fileNameHeaderText", "Имя");
        UIManager.put("FileChooser.fileSizeHeaderText", "Размер");
        UIManager.put("FileChooser.detailsViewActionLabelText", "Детальный");
        UIManager.put("FileChooser.listViewActionLabelText", "Списком");
        UIManager.put("FileChooser.refreshActionLabelText", "Обновить");
        UIManager.put("FileChooser.viewMenuLabelText", "Вид");

        fc = new FileChooser();
        add(fc, BorderLayout.CENTER);

        fc.setFileSelectionMode(mode.id);
        fc.setMultiSelectionEnabled(false);

        fireInit();

        if (fname.length() > 0) {
            File f = new File(fname);
            fc.setCurrentDirectory(f);
            fc.setSelectedFile(f);
        }
        pack();
    }

    /**
     * Вызывается при инициализации, до выбора тек.файла.
     */
    public void fireInit() {
    }

    /**
     * Вызывается при выборе файла.
     */
    public void fireApply() {
        fc.approveSelection();
    }

    /**
     * Вызывается при отмене выбора.
     */
    public void fireCancel() {
        fc.cancelSelection();
    }

    private class FileChooser extends JFileChooser {

        @Override
        public void approveSelection() {
            File f = getSelectedFile();
            if (f.exists()) {
                fireApply();
            } else {
                if (target == Target.EXIST_ONLY) {
                    JOptionPane.showMessageDialog(this, "Выбранный файл/каталог не существует!");
                } else {
                    try {
                        f.createNewFile();
                        fireApply();
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(this, "Ошибка создания файла/каталога!");
                    }
                }
            }
        }

        @Override
        public void cancelSelection() {
            fireCancel();
        }
    }
}
