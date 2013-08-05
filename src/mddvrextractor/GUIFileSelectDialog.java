/*
 * Copyright (c) 2011-2013, Aleksey Nikolaevich Dokshin. All right reserved.
 * Contacts: dant.it@gmail.com, dokshin@list.ru.
 */
package mddvrextractor;

import mddvrextractor.gui.GUIDialog;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import mddvrextractor.gui.GUI;

/**
 * Диалог выбора файла/каталога (для чтения/записи).
 *
 * @author Докшин Алексей Николаевич <dant.it@gmail.com>
 */
public class GUIFileSelectDialog extends GUIDialog {

    /**
     * Компонент выбора файла.
     */
    private FileChooser fileChooser;
    /**
     * Цель диалога.
     */
    private Target target;
    /**
     * Режим диалога.
     */
    private Mode mode;
    /**
     * Текстовые ресурсы для интерфейса.
     */
    public static String x_Cancel, x_CancelSelect, x_DatailView, x_Date,
            x_Detail, x_Fefresh, x_FileName, x_FileType, x_GoUp, x_Home, x_List,
            x_ListView, x_Name, x_Path, x_Select, x_SelectFile, x_Size, x_View,
            x_CantCreate, x_Error, x_NotFound;

    /**
     * Цель вызова диалога
     */
    public static enum Target {

        /**
         * Выбор существующего файла/каталога.
         */
        EXIST_ONLY,
        /**
         * Создание нового файла/перезапись существующего.
         */
        NEW_OR_EXIST
    }

    /**
     * Режим работы диалога.
     */
    public static enum Mode {

        /**
         * Только каталоги.
         */
        DIR(JFileChooser.DIRECTORIES_ONLY),
        /**
         * Только файлы.
         */
        FILE(JFileChooser.FILES_ONLY),
        /**
         * Файлы и каталоги.
         */
        ALL(JFileChooser.FILES_AND_DIRECTORIES);
        /**
         * ID соответствующий режиму JFileChooser.
         */
        public int id;

        /**
         * Конструктор.
         *
         * @param id Код режима.
         */
        private Mode(int id) {
            this.id = id;
        }
    }

    /**
     * Конструктор.
     *
     * @param owner Компонент-владелец диалога (null - без владельца).
     * @param title Название.
     * @param filename Начальное имя файла\каталога.
     * @param defname Имя файла\каталога\маски по умолчанию (если filename не
     * задано).
     * @param target Тип цели.
     * @param mode Режим работы.
     */
    protected GUIFileSelectDialog(Window owner, String title, String filename,
            String defname, Target target, Mode mode) {
        super(owner);

        setTitle(title);

        filename = filename != null ? filename : "";
        this.target = target;
        this.mode = mode;

        UIManager.put("FileChooser.readOnly", target == Target.EXIST_ONLY);
        UIManager.put("FileChooser.cancelButtonText", x_Cancel);
        UIManager.put("FileChooser.cancelButtonToolTipText", x_CancelSelect);
        UIManager.put("FileChooser.detailsViewButtonToolTipText", x_DatailView);
        UIManager.put("FileChooser.listViewButtonToolTipText", x_ListView);
        UIManager.put("FileChooser.fileNameLabelText", x_FileName);
        UIManager.put("FileChooser.filesOfTypeLabelText", x_FileType);
        UIManager.put("FileChooser.homeFolderToolTipText", x_Home);
        UIManager.put("FileChooser.lookInLabelText", x_Path);
        UIManager.put("FileChooser.openButtonText", x_Select);
        UIManager.put("FileChooser.openButtonToolTipText", x_SelectFile);
        UIManager.put("FileChooser.upFolderToolTipText", x_GoUp);
        UIManager.put("FileChooser.fileDateHeaderText", x_Date);
        UIManager.put("FileChooser.fileNameHeaderText", x_Name);
        UIManager.put("FileChooser.fileSizeHeaderText", x_Size);
        UIManager.put("FileChooser.detailsViewActionLabelText", x_Detail);
        UIManager.put("FileChooser.listViewActionLabelText", x_List);
        UIManager.put("FileChooser.refreshActionLabelText", x_Fefresh);
        UIManager.put("FileChooser.viewMenuLabelText", x_View);

        fileChooser = new FileChooser();
        add(fileChooser, BorderLayout.CENTER);

        fileChooser.setFileSelectionMode(mode.id);
        fileChooser.setMultiSelectionEnabled(false);

        fireInit(fileChooser);

        if (filename.length() > 0) {
            File f = new File(filename);
            fileChooser.setCurrentDirectory(f);
            fileChooser.setSelectedFile(f);
        } else if (defname.length() > 0) {
            File f = new File(defname);
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
     *
     * @param fc Передача компонента выбора файла.
     */
    public void fireInit(FileChooser fc) {
    }

    /**
     * Вызывается при выборе файла.
     *
     * @param fc Передача компонента выбора файла.
     * @exception CancelActionExeption Вызывается при необходимости отмены
     * действия.
     */
    public void fireApply(FileChooser fc) throws CancelActionExeption {
    }

    /**
     * Вызывается при отмене выбора.
     *
     * @param fc Передача компонента выбора файла.
     * @exception CancelActionExeption Вызывается при необходимости отмены
     * действия.
     */
    public void fireCancel(FileChooser fc) throws CancelActionExeption {
    }

    /**
     * Расширение компонента выбора файла.
     */
    protected class FileChooser extends JFileChooser {

        public FileChooser() {
            ArrayList<Component> listComponents = GUI.listComponents(this, JButton.class);
            for (Component c : listComponents) {
                JButton b = (JButton) c;
                if ("SynthFileChooser.approveButton".equals(b.getName())
                        || "SynthFileChooser.cancelButton".equals(b.getName())) {
                    b.setForeground(Color.WHITE);
                    b.setBackground(GUI.c_Base);
                }
            }
        }

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
                    JOptionPane.showMessageDialog(this, x_NotFound, x_Error,
                            JOptionPane.ERROR_MESSAGE);
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
                        JOptionPane.showMessageDialog(this, x_CantCreate,
                                x_Error, JOptionPane.ERROR_MESSAGE);
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
