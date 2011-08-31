/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dvrextract;

import dvrextract.gui.GUI;
import dvrextract.gui.JExtComboBox;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import net.miginfocom.swing.MigLayout;

/**
 * Вкладка "Источник".
 * @author lex
 */
public class GUI_TabSource extends JPanel implements ActionListener {

    JTextField textSource;
    JButton buttonSource;
    JTextField textType;
    JExtComboBox comboCam;
    JPanel panelFiles;
    JPanel panelImage;
    JPanel panelInfo;

    public GUI_TabSource() {
        init();
    }

    public void init() {
        setLayout(new MigLayout("debug", "", "[]2[][fill, grow]"));

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
        comboCam.addItem(1, "не выбрана");
        comboCam.showData();
        
        JPanel panel = new JPanel(new MigLayout("fill, ins 0", "[]5[]", "[]5[]"));
        panel.setBackground(Color.red);
        add(panel, "span, grow");
        
        // Панель отображения файлов источника.
        panelFiles = new JPanel(new MigLayout("fill, ins 0 0 0 3"));
        panelFiles.setBackground(Color.blue);
        panel.add(panelFiles, "spany 2");
        // Панель отображения первого базового кадра файла.
        panelImage = new JPanel(new MigLayout("fill, ins 0"));
        panelImage.setBackground(Color.green);
        panel.add(panelImage, "wrap");
        // Панель отображения информации о файле.
        panelInfo = new JPanel(new MigLayout("fill, ins 0"));
        panelInfo.setBackground(Color.cyan);
        panel.add(panelInfo, "");
        
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == buttonSource) {
            GUI_SourceSelect dlg = new GUI_SourceSelect();
            dlg.center();
            dlg.setVisible(true);
        }
    }

    public void setSource(String src, FileType type) {
        textSource.setText(src);
        textType.setText(type.title);
    }

    public void setCams(ArrayList<Integer> cams) {
        comboCam.removeItems();
        for (int i : cams) {
            comboCam.addItem(i, i == 0 ? "не выбрана" : "CAM" + i);
        }
        comboCam.showData();
    }

    public void setCams(int cam) {
        comboCam.removeItems();
        comboCam.addItem(cam, cam == 0 ? "не выбрана" : "CAM" + cam);
        comboCam.showData();
    }
}
