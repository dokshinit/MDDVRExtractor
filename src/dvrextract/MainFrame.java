/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dvrextract;

import java.awt.BorderLayout;
import java.awt.Color;
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

    JTextField textSource;
    JButton buttonSource;
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

        TabPane tabPane = new TabPane();
        
        ////////////////////////////////////////////////////////////////////////
        // Вкладка "Источник"
        ////////////////////////////////////////////////////////////////////////
        JPanel ptabSource = new JPanel(new MigLayout());
        ptabSource.add(new JLabel("Источник"));
        textSource = new JTextField(50);
        ptabSource.add(textSource, "growx");
        buttonSource = new JButton("Выбор");
        ptabSource.add(buttonSource, "wrap");
        JPanel panelSrcInfo = new JPanel(new MigLayout());
        
        panelSrcInfo.add(new JLabel("Тип:"));
        JTextField textType = new JTextField("не определён", 20);
        textType.setEditable(false);
        //textType.setEnabled(false);
        panelSrcInfo.add(textType);
        panelSrcInfo.add(new JLabel("Камера:"));
        JComboBox comboCam = new JComboBox();
        panelSrcInfo.add(comboCam, "wrap");
        ptabSource.add(panelSrcInfo, "span, grow");
        
        tabPane.addTab("Источник", ptabSource);
        //tabPane.setEnabledAt(1,false);
        
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
            p.setImage(ImageIO.read(is));

        } catch (IOException e) {
            e.printStackTrace();
        }

        //textInput = new JTextField(50);
        //buttonInput = new JButton("Выбор");


        textVideo = new JTextField(50);
        buttonVideo = new JButton("Выбор");

        comboCams = new JComboBox();
        checkAudio = new JCheckBox("Включать аудиоданные.");

        buttonInfo = new JButton("Информация");

        buttonProcess = new JButton("Обработка");

        progressBar = new JProgressBar();

        add(tabPane, BorderLayout.CENTER);

        JPanel panelButton = new JPanel(new MigLayout("","[grow,fill][10px]"));
        JLabel lInfo = new JLabel("Инфо:");
        panelButton.add(lInfo);
        panelButton.add(buttonProcess, "spany 2, growy, wrap");
        panelButton.add(progressBar);
        add(panelButton, BorderLayout.SOUTH);
        panelButton.setBackground(Color.red);
        

        pack();
    }
}
