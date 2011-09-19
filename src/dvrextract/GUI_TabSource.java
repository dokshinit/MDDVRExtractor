/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dvrextract;

import dvrextract.gui.GUI;
import dvrextract.gui.JExtComboBox;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import net.miginfocom.swing.MigLayout;

/**
 * Вкладка "Источник".
 * @author lex
 */
public final class GUI_TabSource extends JPanel implements ActionListener {

    private JTextField textSource;
    private JButton buttonSource;
    private JTextField textType;
    private JExtComboBox comboCam;
    //
    private GUIFilesPanel filesPanel;
    private GUIFileInfoPanel infoPanel;

    public GUI_TabSource() {
        init();
    }

    public void init() {
        setLayout(new MigLayout("", "", "[]2[][fill, grow]"));

        add(GUI.createLabel("Источник"));
        add(textSource = GUI.createText(300), "growx, span, split 2");
        textSource.setEditable(false);
        add(buttonSource = GUI.createButton("Выбор"), "wrap");
        buttonSource.addActionListener(this);

        // Отображение типа источника.
        add(GUI.createLabel("Тип:"), "right");
        add(textType = GUI.createText("не определён", 10), "span, split 3");
        textType.setEditable(false);
        textType.setHorizontalAlignment(JTextField.CENTER);

        // Выбор камеры для обработки.
        add(GUI.createLabel("Камера:"), "");
        add(comboCam = GUI.createCombo(false), "w 110, wrap");
        comboCam.addActionListener(this);
        comboCam.addItem(1, "не выбрана");
        comboCam.showData();

        //JPanel panel = new JPanel(new MigLayout("debug, fill, ins 0", "[100:300:]5[352:352:704]", "[288:288:576]5[]"));
        JPanel panel = new JPanel(new BorderLayout());
        add(panel, "span, grow");

        // Панель отображения файлов источника.
        filesPanel = new GUIFilesPanel();
        filesPanel.setBackground(Color.blue);
        filesPanel.setMinimumSize(new Dimension(200, 100));
        // Панель отображения информации о файле.
        infoPanel = new GUIFileInfoPanel();
        infoPanel.setBackground(Color.cyan);
        //
        JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT, filesPanel, infoPanel);
        sp.setDividerSize(8);
        panel.add(sp, BorderLayout.CENTER);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == comboCam) {
            do_CamSelect();
        } else if (e.getSource() == buttonSource) {
            do_SourceSelect();
        }
    }

    public void do_SetDisplayCams(ArrayList<Integer> cams) {
        comboCam.removeItems();
        if (cams != null) {
            if (cams.isEmpty()) {
                comboCam.addItem(0, "не выбрана");
            }
            for (int i : cams) {
                comboCam.addItem(i, i == 0 ? "не выбрана" : "CAM" + i);
            }
        }
        comboCam.showData();
        do_CamSelect();
    }

    public void do_SetDisplayCams(int cam) {
        ArrayList<Integer> list = new ArrayList<Integer>();
        list.add(cam);
        do_SetDisplayCams(list);
    }

    private void do_SourceSelect() {
        GUI_SourceSelect dlg = new GUI_SourceSelect();
        dlg.center();
        dlg.setVisible(true);
        //
        textSource.setText(App.srcName);
        textType.setText(App.srcType.title);
    }

    public void do_CamSelect() {
        App.srcCamSelect = comboCam.getSelectedItem().id;
        filesPanel.selectCamModel(App.srcCamSelect);
    }
}
