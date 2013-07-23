/*
 * Copyright (c) 2011-2012, Aleksey Nikolaevich Dokshin. All right reserved.
 * Contacts: dant.it@gmail.com, dokshin@list.ru.
 */
package mddvrextract;

import mddvrextract.gui.GUI;
import mddvrextract.gui.GUIDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import net.miginfocom.swing.MigLayout;

/**
 * Диалог отображения информации о предполагаемом объеме данных.
 *
 * @author Докшин Алексей Николаевич <dant.it@gmail.com>
 */
public class GUIEvaluationInfoDialog extends GUIDialog implements ActionListener {

    /**
     * Кнопка закрытия окна.
     */
    protected JButton buttonCancel;
    /**
     * Текстовые ресурсы для интерфейса.
     */
    public static String x_Title, x_Period1, x_Period2, x_Note, x_Close;

    /**
     * Конструктор.
     *
     * @param owner Окно - владелец. Если null - без блокировки.
     */
    public GUIEvaluationInfoDialog() {
        super();
        setModal(true);
        setTitle(x_Title);
        setLayout(new MigLayout("fill"));

        // Источник (камера):
        addPair(DataCalculator.x_Cam, App.gui.tabProcess.getSelectedCamText(), 10);
        // Период (камера):
        add(new JLabel(x_Period1), "right");
        add(createText(App.gui.tabProcess.getDateStart().getText(), 15));
        add(new JLabel(x_Period2));
        add(createText(App.gui.tabProcess.getDateEnd().getText(), 15), "wrap");
        // Длительность:
        addPair(DataCalculator.x_Duration, DataCalculator.getDurationAsText(), 20);
        // Объём видео:
        addPair(DataCalculator.x_VideoSize, DataCalculator.getVideoSizeAsText(), 20);
        // Объём аудио:
        addPair(DataCalculator.x_AudioSize, DataCalculator.getAudioSizeAsText(), 20);
        // Примечание:
        add(GUI.createNoteLabel(x_Note), "span, growx, center, wrap");

        add(buttonCancel = GUI.createButton(x_Close), "spanx, center");
        buttonCancel.addActionListener(GUIEvaluationInfoDialog.this);

        pack();

        setResizable(false); // После вычисления размера - изменение ни к чему.
    }

    private JTextField createText(String text, int size) {
        JTextField t = new JTextField(text);
        t.setEditable(false);
        return t;
    }
    
    private void addPair(String label, String text, int size) {
        add(new JLabel(label), "right");
        add(createText(text, size), "spanx, wrap");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == buttonCancel) {
            dispose();
        }
    }

}
