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
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import javax.imageio.ImageIO;
import javax.swing.JLabel;
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
    private JScrollPane scrollPane;
    private JPanel panelInfo;
    private GUIImagePanel panelImage;
    // Поля для отображения информации о файле
    private JTextField textName;
    private JTextField textSize;
    private JTextField textType;
    private JTextField textCams;
    private JTextField textFirstTime;
    private JTextField textLastTime;
    private JTextField textAmountTime;
    //
    private FileInfo curInfo;
    private static SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    public GUIFileInfoPanel() {
        init();
    }

    private JTextField createInfo(String title, int size, String add) {
        JTextField tf = GUI.createText(size);
        panelInfo.add(GUI.createLabel(title));
        panelInfo.add(tf, add != null ? add + ",wrap" : "wrap");
        tf.setEditable(false);
        return tf;
    }

    public void init() {
        setLayout(new BorderLayout());
        //
        panelImage = new GUIImagePanel("НЕТ");
        JLabel l = panelImage.getLabel();
        l.setFont(new Font(l.getFont().getName(), Font.BOLD, 100));
        l.setForeground(Color.WHITE);
        panelImage.setBackground(new Color(0x8080FF));
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

    public void displayInfo(FileInfo info) {
        if (info == null) {
            textName.setText("");
            textSize.setText("");
            textType.setText("");
            textCams.setText("");
            textFirstTime.setText("");
            textLastTime.setText("");
            textAmountTime.setText("");
            panelImage.setImage(null);
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
                Frame f = Files.getFirstMainFrame(info, App.srcCamSelect);
                try {
                    if (f != null) {
                        InputData in = new InputData(info.fileName);
                        in.seek(f.pos + f.getHeaderSize());
                        byte[] ba = new byte[f.videoSize];
                        in.read(ba, f.videoSize);

                        Process pr = Runtime.getRuntime().exec("ffmpeg -i - -r 1 -s 352x288 -f image2 -");
                        InputStream is = pr.getInputStream();
                        OutputStream os = pr.getOutputStream();
                        os.write(ba, 0, ba.length);
                        os.close();
                        BufferedImage image = ImageIO.read(is);
                        panelImage.setImage(image);
                        pr.destroy();
                    } else {
                        panelImage.setImage(null);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
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
