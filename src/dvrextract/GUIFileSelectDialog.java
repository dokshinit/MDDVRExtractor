package dvrextract;

import dvrextract.gui.GUIDialog;
import java.awt.BorderLayout;
import java.awt.Window;
import java.io.File;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

/**
 * Диалог выбора файла/каталога (для чтения/записи).
 * @author lex
 */
public class GUIFileSelectDialog extends GUIDialog {

    // Компонент выбора файла.
    private FileChooser fileChooser;
    // Цель диалога.
    private Target target;
    // Режим диалога.
    private Mode mode;

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

    protected GUIFileSelectDialog(Window owner, String title, String fname, Target tgt, Mode m) {
        super(owner);
        
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

        fileChooser = new FileChooser();
        add(fileChooser, BorderLayout.CENTER);
        
        fileChooser.setFileSelectionMode(mode.id);
        fileChooser.setMultiSelectionEnabled(false);

        fireInit(fileChooser);

        if (fname.length() > 0) {
            File f = new File(fname);
            fileChooser.setCurrentDirectory(f);
            fileChooser.setSelectedFile(f);
        }
        pack();
    }

    /**
     * Исключение инициируемое при отмене действия диалога (выбор\отмена).
     */
    public static class CancelActionExeption extends Exception {
    }

    /**
     * Вызывается при инициализации, до выбора тек.файла.
     */
    public void fireInit(FileChooser fc) {
    }

    /**
     * Вызывается при выборе файла.
     * @exception CancelActionExeption Вызывается при необходимости отмены действия.
     */
    public void fireApply(FileChooser fc) throws CancelActionExeption {
    }

    /**
     * Вызывается при отмене выбора.
     * @exception CancelActionExeption Вызывается при необходимости отмены действия.
     */
    public void fireCancel(FileChooser fc) throws CancelActionExeption {
    }

    protected class FileChooser extends JFileChooser {

        @Override
        public void approveSelection() {
            File f = getSelectedFile();
            if (f.exists()) {
                try {
                    fireApply(fileChooser);
                    super.approveSelection();
                    dispose();
                } catch (CancelActionExeption ex) {
                }
            } else {
                if (target == Target.EXIST_ONLY) {
                    JOptionPane.showMessageDialog(this, "Выбранный файл/каталог не существует!",
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                } else {
                    try {
                        try {
                            if (f.createNewFile()) { // Если не было и был создан - удаляем.
                                f.delete();
                            }
                            fireApply(fileChooser);
                            super.approveSelection();
                            dispose();
                        } catch (CancelActionExeption ex) {
                        }
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(this, "Файл/каталог не может быть создан!",
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }

        @Override
        public void cancelSelection() {
            try {
                fireCancel(fileChooser);
                super.cancelSelection();
                dispose();
            } catch (CancelActionExeption ex) {
            }
        }
    }
}
