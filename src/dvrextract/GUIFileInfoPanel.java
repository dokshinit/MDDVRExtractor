/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dvrextract;

import dvrextract.gui.GUI;
import dvrextract.gui.GUIImagePanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import net.miginfocom.swing.MigLayout;
import util.DateTools;
import util.NumberTools;

/**
 * Панель с информацией о выбранном файле-источнике.
 * @author lex
 */
public final class GUIFileInfoPanel extends JPanel {

    // Скролл для всей инфо-панели.
    JScrollPane scrollPane;
    JPanel panelInfo;
    GUIImagePanel panelImage;
    // Поля для отображения информации о файле
    JTextField textName;
    JTextField textSize;
    JTextField textType;
    JTextField textCams;
    JTextField textFirstTime;
    JTextField textLastTime;
    JTextField textAmountTime;
    //
    FileInfo curInfo;

    public GUIFileInfoPanel() {
        init();
    }

    JTextField createInfo(String title, int size, String add) {
        JTextField tf = GUI.createText(size);
        panelInfo.add(GUI.createLabel(title));
        panelInfo.add(tf, add != null ? add + ",wrap" : "wrap");
        tf.setEditable(false);
        return tf;
    }

    public void init() {
        setLayout(new BorderLayout());
        //
        panelImage = new GUIImagePanel();
        panelImage.setBackground(Color.red);
        Dimension d = new Dimension(352, 288);
        panelImage.setPreferredSize(d);
        panelImage.setMinimumSize(d);
        panelImage.setMaximumSize(d);
        //
        panelInfo = new JPanel(new MigLayout("", "[]5[right][grow,shrink]"));
        panelInfo.add(panelImage, "spany");
        //
        textName = createInfo("Имя", 30, "growx");
        textSize = createInfo("Размер", 15, null);
        textType = createInfo("Тип", 10, null);
        textCams = createInfo("Камеры", 30, null);
        textFirstTime = createInfo("Начало", 15, null);
        textLastTime = createInfo("Конец", 15, null);
        textAmountTime = createInfo("Длительность", 22, null);
        //
        scrollPane = new JScrollPane(panelInfo);
        add(scrollPane, BorderLayout.CENTER);
    }
    static SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    public void displayInfo(FileInfo info) {
        if (info == null) {
            textName.setText("");
            textSize.setText("");
            textType.setText("");
            textCams.setText("");
            textFirstTime.setText("");
            textLastTime.setText("");
            textAmountTime.setText("");
        } else {
            if (curInfo != info) {
                textName.setText(info.fileName);
                textSize.setText(NumberTools.doubleToFormatString((double) info.fileSize, NumberTools.format0, "", "") + " байт");
                textType.setText(info.fileType.title);
                textCams.setText(info.getCamsToString());
                if (info.frameFirst != null) {
                    textFirstTime.setText(df.format(info.frameFirst.time));
                } else {
                    textFirstTime.setText("");
                }
                if (info.frameLast != null) {
                    textLastTime.setText(df.format(info.frameLast.time));
                } else {
                    textLastTime.setText("");
                }
                if (info.frameFirst != null && info.frameLast != null) {
                    textAmountTime.setText(timeToString(info.frameLast.time.getTime() - info.frameFirst.time.getTime()));
                } else {
                    textAmountTime.setText("");
                }
                if (info.frameFirst != null && info.frameFirst.isParsed && info.frameFirst.isMainFrame) {
                    
                    try {
                        InputData in = new InputData(info.fileName);
                        in.seek(info.frameFirst.pos + info.frameFirst.getHeaderSize());
                        byte[] ba = new byte[info.frameFirst.videoSize+1];
                        in.read(ba, info.frameFirst.videoSize);
                        
                        Process pr = Runtime.getRuntime().exec("ffmpeg -i - -r 1 -s 352x288 -f image2 -");
                        InputStream is = pr.getInputStream();
                        OutputStream os = pr.getOutputStream();
                        os.write(ba, 0, ba.length-1);
                        os.close();
                        BufferedImage image = ImageIO.read(is);
                        panelImage.setImage(image);
                        pr.destroy();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
        curInfo = info;
    }

    public String timeToString(long period) {
        long h = (period) / (3600 * 1000);
        long m = (period - h * 3600 * 1000) / (60 * 1000);
        long s = (period - h * 3600 * 1000 - m * 60 * 1000) / (1000);
        long ms = (period - h * 3600 * 1000 - m * 60 * 1000 - s * 1000);
        return String.format("%d час. %d мин. %d сек. %d мсек.", h, m, s, ms);
    }
}
