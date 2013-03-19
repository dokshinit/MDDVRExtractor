/*
 * Copyright (c) 2011-2012, Aleksey Nikolaevich Dokshin. All right reserved.
 * Contacts: dant.it@gmail.com, dokshin@list.ru.
 */
package mddvrextract;

import mddvrextract.gui.GUI;
import mddvrextract.gui.JExtComboBox;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import net.miginfocom.swing.MigLayout;

/**
 * Вкладка "Источник".
 *
 * @author Докшин Алексей Николаевич <dant.it@gmail.com>
 */
public final class GUI_TabSource extends JPanel implements ActionListener {

    /**
     * Надпись Источник.
     */
    private JLabel labelSource;
    /**
     * Путь к источнику.
     */
    private JTextField textSource;
    /**
     * Кнопка вызова диалога выбора источника.
     */
    private JButton buttonSource;
    /**
     * Надпись Тип.
     */
    private JLabel labelType;
    /**
     * Тип источника.
     */
    private JTextField textType;
    /**
     * Надпись Камера.
     */
    private JLabel labelCam;
    /**
     * Список камер для выбора.
     */
    private JExtComboBox comboCam;
    /**
     * Панель списка файлов-источников.
     */
    private GUIFilesPanel filesPanel;
    /**
     * Панель отображения информации о файле-источнике.
     */
    private GUIFileInfoPanel infoPanel;
    /**
     * Текстовые ресурсы для интерфейса.
     */
    public static String x_Cam, x_NotSelected, x_Select, x_Source, x_Type;

    /**
     * Конструктор.
     */
    public GUI_TabSource() {
        labelSource = GUI.createLabel(x_Source);
        textSource = GUI.createText(300);
        buttonSource = GUI.createButton(x_Select);
        buttonSource.addActionListener(GUI_TabSource.this);
        labelType = GUI.createLabel(x_Type);
        textType = GUI.createText(App.Source.getType().title, 10);
        labelCam = GUI.createLabel(x_Cam);
        comboCam = GUI.createCombo();
        comboCam.addItem(0, x_NotSelected);
        comboCam.showData();
        comboCam.addActionListener(GUI_TabSource.this);

        infoPanel = new GUIFileInfoPanel();
        filesPanel = new GUIFilesPanel(infoPanel);
    }

    /**
     * Инициализация графических компонент.
     */
    public void createUI() {
        removeAll();

        setLayout(new MigLayout("", "", "[]2[][fill, grow]"));

        add(labelSource);
        add(textSource, "growx, span, split 2");
        textSource.setEditable(false);
        add(buttonSource, "wrap");

        // Отображение типа источника.
        add(labelType, "right");
        add(textType, "span, split 3");
        textType.setEditable(false);
        textType.setHorizontalAlignment(JTextField.CENTER);

        // Выбор камеры для обработки.
        add(labelCam, "gapleft 20");
        add(comboCam, "w 110, wrap");

        JPanel panel = new JPanel(new BorderLayout());
        add(panel, "span, grow");

        // Панель отображения информации о файле.
        infoPanel.setMinimumSize(new Dimension(300, 90));
        // Панель отображения файлов источника.
        filesPanel.setBackground(Color.blue);
        filesPanel.setMinimumSize(new Dimension(300, 90));

        JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT, filesPanel, infoPanel);
        sp.setDividerSize(8);
        panel.add(sp, BorderLayout.CENTER);
    }

    /**
     * Актуализация контента при смене языка отображения.
     */
    public void updateLocale() {
        labelSource.setText(x_Source);
        buttonSource.setText(x_Select);
        labelType.setText(x_Type);
        textType.setText(App.Source.getType().title);
        labelCam.setText(x_Cam);
        validateCamsListChange();
        //
        filesPanel.updateLocale();
        infoPanel.updateLocale();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == comboCam) {
            fireCamSelect();
        } else if (e.getSource() == buttonSource) {
            fireSelectSource();
        }
    }

    /**
     * Вызывается при изменении источнкика. Актуализацирует отображение
     * источника и списка камер.
     */
    public void validateSourceChange() {
        GUI.InSwingWait(new Runnable() {

            @Override
            public void run() {
                // Отображаем новый источник и его тип.
                textSource.setText(App.Source.getName().name);
                textType.setText(App.Source.getType().title);
                // Подразумевается, что инфа о камерах уже актуальная (очищена).
                // Сигнализируем о изменении списка камер.
                validateCamsListChange();
            }
        });
    }

    /**
     * Вызывается при изменении списка камер источника. Актуализирует
     * отображение и выбранную камеру.
     */
    public void validateCamsListChange() {
        GUI.InSwingWait(new Runnable() {

            @Override
            public void run() {
                int id = comboCam.getSelectedItem().id;

                comboCam.removeItems();
                for (int i = 1; i <= App.MAXCAMS; i++) {
                    CamInfo ci = App.Source.getCamInfo(i);
                    if (ci.isExists) {
                        comboCam.addItem(i, "CAM" + i);
                    }
                }
                // Если нет камер - ставим не выбрано.
                if (comboCam.getListItemCount() == 0) {
                    comboCam.addItem(0, x_NotSelected);
                }
                comboCam.showData();
                if (id > 0) { // Если была выбрана камера - пытаемся выбрать её же.
                    comboCam.setSelectedId(id);
                }

                fireCamSelect(); // Выбор первой по списку камеры.
            }
        });
    }

    /**
     * Выставление блокировок элементов согласно текущему состоянию. Отдельно не
     * вызывается, только из основного окна (пожтому нет тредсейф).
     */
    public void validateLocks() {
        GUI.InSwingLater(new Runnable() {

            @Override
            public void run() {
                if (Task.isAlive()) {
                    // Выполняется задача.
                    buttonSource.setEnabled(false);
                } else {
                    // Задач нет.
                    buttonSource.setEnabled(true);
                }
            }
        });
    }

    /**
     * Обработка выбора источника (запуск диалога).
     */
    private void fireSelectSource() {
        GUI_SourceSelect dlg = new GUI_SourceSelect();
        GUI.centerizeFrame(dlg, App.gui);
        dlg.setVisible(true);
    }

    /**
     * Обработка выбора камеры из списка.
     */
    private void fireCamSelect() {
        App.Source.setSelectedCam(comboCam.getSelectedItem().id);
        App.gui.tabProcess.validateSelectedCam(comboCam.getSelectedItem().object.toString());
        filesPanel.setModel(App.Source.getSelectedCam());
    }
}
