/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dvrextract;

import dvrextract.gui.GUI;
import dvrextract.gui.GUIImagePanel;
import dvrextract.gui.JExtComboBox;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import net.miginfocom.swing.MigLayout;

/**
 * Вкладка "Источник".
 * @author lex
 */
public final class GUI_TabSource extends JPanel implements ActionListener {

    JTextField textSource;
    JButton buttonSource;
    JTextField textType;
    JExtComboBox comboCam;
    //
    GUIFilesPanel filesPanel;
    GUIImagePanel imagePanel;
    JPanel panelInfo;

    public GUI_TabSource() {
        init();
    }

    public void init() {
        setLayout(new MigLayout("", "", "[]2[][fill, grow]"));

        add(GUI.createLabel("Источник"));
        add(textSource = GUI.createText(100), "growx, span, split 2");
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
        //panel.setBackground(Color.red);
        add(panel, "span, grow");

        // Панель отображения файлов источника.
        filesPanel = new GUIFilesPanel();
        filesPanel.setBackground(Color.blue);
        filesPanel.setMinimumSize(new Dimension(300, 200));
        //panel.add(panelFiles, "spany 2, top");
        // Панель отображения первого базового кадра файла.
        imagePanel = new GUIImagePanel();
        imagePanel.setBackground(Color.green);
        imagePanel.setMinimumSize(new Dimension(352, 288));
        imagePanel.setPreferredSize(new Dimension(352, 288));
        imagePanel.setMaximumSize(new Dimension(352, 288));
        //panel.add(panelImage, "wrap");
        // Панель отображения информации о файле.
        panelInfo = new JPanel(new MigLayout("fill, ins 5"));
        panelInfo.setBackground(Color.cyan);
        panelInfo.add(imagePanel, "span, growx, left, top");
        //panel.add(panelInfo, "");
        JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, filesPanel, panelInfo);
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

    public void displaySource(String src, FileType type) {
        textSource.setText(src);
        textType.setText(type.title);
    }

    public void displayCams(ArrayList<Integer> cams) {
        int id = comboCam.getSelectedItem().id;
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
        int newid = comboCam.getSelectedItem().id;
        if (id != newid) {
            // Применяем выбор.
            filesPanel.selectCamModel(newid);
        }
    }

    public void displayCams(int cam) {
        ArrayList<Integer> list = new ArrayList<Integer>();
        list.add(cam);
        displayCams(list);
    }

    private void do_SourceSelect() {
        GUI_SourceSelect dlg = new GUI_SourceSelect();
        dlg.center();
        dlg.setVisible(true);
    }

    public void do_CamSelect() {
        int newid = comboCam.getSelectedItem().id;
        filesPanel.selectCamModel(newid);
    }
}
