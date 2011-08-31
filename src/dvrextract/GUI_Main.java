/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dvrextract;

import dvrextract.gui.GUI;
import dvrextract.gui.GUITabPane;
import dvrextract.gui.GUIFrame;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
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
    public GUI_TabSource tabSource;
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

    private void init() {
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setPreferredSize(new Dimension(800, 600));

        GUITabPane tabPane = new GUITabPane();

        ////////////////////////////////////////////////////////////////////////
        // Вкладка "Источник"
        ////////////////////////////////////////////////////////////////////////
        tabPane.addTab("Источник", tabSource = new GUI_TabSource());

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
            //tabSource.panelImage.add(p, "span, growx");

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
    }
}
