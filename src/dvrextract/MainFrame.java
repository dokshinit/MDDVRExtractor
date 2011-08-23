/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dvrextract;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
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
public class MainFrame extends JFrame {

    JTextField textInput;
    JButton buttonInput;
    //
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
    //
    JButton buttonInfo; // Запуск\остановка сбора информации.
    JButton buttonProcess; // Запуск\остановка обработки.
    //
    JProgressBar progressBar; // Прогрес выполнения операции.

    public MainFrame() {
        init();
    }

    void init() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(700, 550));

        JTabbedPane tabPane = new JTabbedPane();
        JPanel panelTab1 = new JPanel(new MigLayout());
        tabPane.addTab("Управление", panelTab1);
        JPanel panelTab2 = new JPanel(new MigLayout());
        tabPane.addTab("Источник", panelTab2);
        JPanel panelTab3 = new JPanel(new MigLayout());
        tabPane.addTab("Состояние", panelTab3);
        JPanel panelTab4 = new JPanel(new MigLayout());
        tabPane.addTab("Лог", panelTab4);
        add(tabPane, BorderLayout.CENTER);

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
            p.setImage(ImageIO.read(is));

        } catch (IOException e) {
            e.printStackTrace();
        }

        //textInput = new JTextField(50);
        //buttonInput = new JButton("Выбор");

        textRaw = new JTextField(50);
        buttonRaw = new JButton("Выбор");
        panelTab1.add(new JLabel("Источник"), "skip");
        panelTab1.add(textRaw);
        panelTab1.add(buttonRaw, "wrap");
        panelTab1.add(p, "span");

        textVideo = new JTextField(50);
        buttonVideo = new JButton("Выбор");

        comboCams = new JComboBox();
        checkAudio = new JCheckBox("Включать аудиоданные.");

        buttonInfo = new JButton("Информация");

        buttonProcess = new JButton("Обработка");

        progressBar = new JProgressBar();
        add(progressBar, BorderLayout.SOUTH);

        pack();
    }
}
