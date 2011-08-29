/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dvrextract;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
        setPreferredSize(new Dimension(700, 550));

        GUITabPane tabPane = new GUITabPane();
        
        ////////////////////////////////////////////////////////////////////////
        // Вкладка "Источник"
        ////////////////////////////////////////////////////////////////////////
        JPanel ptabSource = new JPanel(new MigLayout());
        ptabSource.add(GUI.createLabel("Источник"));
        ptabSource.add(textSource = GUI.createText(50), "growx");
        ptabSource.add(buttonSource = GUI.createButton("Выбор"), "wrap");
        
        JPanel panelSrcInfo = new JPanel(new MigLayout("fill"));
        // Отображение типа источника.
        panelSrcInfo.add(GUI.createLabel("Тип:"), "skip");
        panelSrcInfo.add(textType = GUI.createText("не определён", 10), "skip");
        textType.setEditable(false);
        textType.setHorizontalAlignment(JTextField.CENTER);
        // Выбор камеры для обработки.
        panelSrcInfo.add(GUI.createLabel("Камера:"), "skip");
        panelSrcInfo.add(comboCam = GUI.createCombo(false), "skip, push, wrap");
        comboCam.addItem(1, "не выбрана");
        comboCam.showData();
        //panelSrcInfo.add(GUI.createButton("Сканировать"), );
        ptabSource.add(panelSrcInfo, "span, grow");
        
        tabPane.addTab("Источник", ptabSource);
        
        App.log("BgColor="+getBackground().toString());
        
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

        JPanel panelButton = new JPanel(new MigLayout("","[grow,fill][10px]"));
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
}
