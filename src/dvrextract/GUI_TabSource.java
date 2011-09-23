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
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import net.miginfocom.swing.MigLayout;

/**
 * Вкладка "Источник".
 * @author lex
 */
public final class GUI_TabSource extends JPanel implements ActionListener {

    // Путь к источнику.
    private JTextField textSource;
    // Кнопка вызова диалога выбора источника.
    private JButton buttonSource;
    // Тип источника.
    private JTextField textType;
    // Список камер для выбора.
    private JExtComboBox comboCam;
    // Панель списка файлов-источников.
    private GUIFilesPanel filesPanel;
    // Панель отображения информации о файле-источнике.
    private GUIFileInfoPanel infoPanel;

    /**
     * Конструктор.
     */
    public GUI_TabSource() {
        init();
    }

    /**
     * Инициализация графических компонент.
     */
    private void init() {
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

        JPanel panel = new JPanel(new BorderLayout());
        add(panel, "span, grow");

        // Панель отображения информации о файле.
        infoPanel = new GUIFileInfoPanel();
        infoPanel.setBackground(Color.cyan);
        infoPanel.setMinimumSize(new Dimension(300, 90));
        // Панель отображения файлов источника.
        filesPanel = new GUIFilesPanel(infoPanel);
        filesPanel.setBackground(Color.blue);
        filesPanel.setMinimumSize(new Dimension(300, 90));
        //
        JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT, filesPanel, infoPanel);
        sp.setDividerSize(8);
        panel.add(sp, BorderLayout.CENTER);
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
     * Обработка выбора источника (запуск диалога).
     */
    private void fireSelectSource() {
        GUI_SourceSelect dlg = new GUI_SourceSelect();
        dlg.center();
        dlg.setVisible(true);
        // Отображаем новый источник и его тип.
        textSource.setText(App.srcName);
        textType.setText(App.srcType.title);
    }

    /**
     * Обработка выбора камеры из списка.
     */
    private void fireCamSelect() {
        App.srcCamSelect = comboCam.getSelectedItem().id;
        filesPanel.setModel(App.srcCamSelect);
    }

    /**
     * Установка списка номеров камер. 
     * При этом происходит выбор первой из списка и обновление списка файлов.
     * @param cams Список номеров камер (для номера =0 - не выбрана).
     */
    public void displayCams(ArrayList<Integer> cams) {
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
        fireCamSelect();
    }

    /**
     * Установка списка номеров камер - из одного номера. 
     * При этом происходит выбор этой камеры и обновление списка файлов.
     * @param cam Номер камеры. (0-не выбрана).
     */
    public void displayCams(int cam) {
        ArrayList<Integer> list = new ArrayList<Integer>();
        list.add(cam);
        displayCams(list);
    }

    /**
     * Установка списка номеров камер исходя из существующих файлов. 
     * При этом происходит выбор первой из списка и обновление списка файлов.
     * @param cams Список номеров камер (для номера =0 - не выбрана).
     */
    public void displayCams() {
        ArrayList<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < App.MAXCAMS; i++) {
            if (App.srcCams[i].isExists) {
                list.add(i + 1);
            }
        }
        displayCams(list);
    }

    /**
     * Разрешение/запрет запуска сканирования.
     * @param state Статус разрешения.
     */
    public void enableScan(boolean state) {
        buttonSource.setEnabled(state);
    }
}
