/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dvrextract;

import dvrextract.gui.GUI;
import dvrextract.gui.GUITabPane;
import dvrextract.gui.GUIFrame;
import dvrextract.gui.JExtComboBox;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.*;
import net.miginfocom.swing.MigLayout;

/**
 * Основное окно приложения.
 * @author lex
 */
public final class GUI_Main extends GUIFrame {
    ////////////////////////////////////////////////////////////////////////////
    // Закладка: Источник
    ////////////////////////////////////////////////////////////////////////////

    JTextField textSource;
    JButton buttonSource;
    JTextField textType;
    JExtComboBox comboCam;
    ////////////////////////////////////////////////////////////////////////////
    // Закладка: Обработка
    ////////////////////////////////////////////////////////////////////////////
    JCheckBox checkRaw; // Создавать сырой файл?
    JTextField textRaw; // Путь и имя файла.
    JButton buttonRaw; // Выбор файла.
    //
    JCheckBox checkVideo; // Создавать видео?
    JTextField textVideo; // Путь и имя файла.
    JButton buttonVideo; // Выбор файла.
    JCheckBox checkSub; // Делать субтитры к видео?
    //
    JComboBox comboCams; // Выбор камеры из списка.
    JCheckBox checkAudio; // Обрабатывать аудио поток.
    ////////////////////////////////////////////////////////////////////////////
    // На всех вкладках
    ////////////////////////////////////////////////////////////////////////////
    JLabel labelInfo; // Строка состояния обработки.
    JButton buttonProcess; // Запуск\остановка обработки.
    JProgressBar progressBar; // Прогрес выполнения операции.

    public GUI_Main() {
        init();
    }

    void init() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(800, 600));

        GUITabPane tabPane = new GUITabPane();

        ////////////////////////////////////////////////////////////////////////
        // Вкладка "Источник"
        ////////////////////////////////////////////////////////////////////////
        JPanel ptabSource = new JPanel(new MigLayout("","","[]2[][]"));
        
        ptabSource.add(GUI.createLabel("Источник"));
        ptabSource.add(textSource = GUI.createText(100), "growx, span, split 2");
        textSource.setEditable(false);
        ptabSource.add(buttonSource = GUI.createButton("Выбор"), "wrap");

        // Отображение типа источника.
        ptabSource.add(GUI.createLabel("Тип:"), "right");
        ptabSource.add(textType = GUI.createText("не определён", 10), "span, split 3");
        textType.setEditable(false);
        textType.setHorizontalAlignment(JTextField.CENTER);
        // Выбор камеры для обработки.
        ptabSource.add(GUI.createLabel("Камера:"), "");
        ptabSource.add(comboCam = GUI.createCombo(false), "w 110, wrap");
        comboCam.addItem(1, "не выбрана");
        comboCam.showData();

        JPanel panelSrcInfo = new JPanel(new MigLayout("fill, ins 0"));
        //panelSrcInfo.add(GUI.createButton("Сканировать"), );
        ptabSource.add(panelSrcInfo, "span, grow");

        tabPane.addTab("Источник", ptabSource);

        ////////////////////////////////////////////////////////////////////////
        // Вкладка "Обработка"
        ////////////////////////////////////////////////////////////////////////
        JPanel ptabProcess = new JPanel(new MigLayout());
        tabPane.addTab("Обработка", ptabProcess);

        ////////////////////////////////////////////////////////////////////////
        // Вкладка "Состояние" (обработки)
        ////////////////////////////////////////////////////////////////////////
        JPanel panelTab3 = new JPanel(new MigLayout());
        tabPane.addTab("Состояние", panelTab3);

        ////////////////////////////////////////////////////////////////////////
        // Вкладка "Лог" (обработки)
        ////////////////////////////////////////////////////////////////////////
        JPanel panelTab4 = new JPanel(new MigLayout());
        tabPane.addTab("Лог", panelTab4);


        ImagePanel p = new ImagePanel();
        p.setPreferredSize(new Dimension(352, 288));
        p.setMinimumSize(new Dimension(352, 288));
        p.setMaximumSize(new Dimension(352, 288));
        try {
            //p.setImage(ImageIO.read(new File("1.jpeg")));
            Process pr = Runtime.getRuntime().exec("ffmpeg -i ./2.frame -r 1 -s 352x288 -f image2 -");
            InputStream is = pr.getInputStream();
            FileOutputStream os = new FileOutputStream("2jpg.out");
            /*
            byte[] bb = new byte[100000];
            while (true) {
            int n = is.read(bb);
            if (n < 0) {
            break;
            }
            os.write(bb, 0, n);
            }
            os.close();
             * 
             */
            BufferedImage image = ImageIO.read(is);
            p.setImage(image);
            panelSrcInfo.add(p, "span, growx");

        } catch (IOException e) {
            e.printStackTrace();
        }

        //textInput = new JTextField(50);
        //buttonInput = new JButton("Выбор");


        textVideo = new JTextField(50);
        buttonVideo = GUI.createButton("Выбор");

        comboCams = new JComboBox();
        checkAudio = new JCheckBox("Включать аудиоданные.");

        buttonProcess = GUI.createButton("Обработка");

        progressBar = new JProgressBar();

        add(tabPane, BorderLayout.CENTER);

        JPanel panelButton = new JPanel(new MigLayout("", "[grow,fill][10px]"));
        JLabel lInfo = GUI.createLabel("Инфо:");
        panelButton.add(lInfo);
        panelButton.add(buttonProcess, "spany 2, growy, wrap");
        panelButton.add(progressBar);
        add(panelButton, BorderLayout.SOUTH);
        panelButton.setBackground(Color.red);


        pack();

        buttonSource.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                GUI_SourceSelect dlg = new GUI_SourceSelect();
                dlg.center();
                dlg.setVisible(true);
            }
        });
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
