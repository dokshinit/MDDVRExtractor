package dvrextract;

import dvrextract.gui.GUI;
import dvrextract.gui.GUIImagePanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
import javax.swing.border.LineBorder;
import net.miginfocom.swing.MigLayout;
import util.NumberTools;

/**
 * Панель с информацией о выбранном файле-источнике.
 * @author lex
 */
public final class GUIFileInfoPanel extends JPanel {

    // Скролл для всей инфо-панели (добавляется в панель - единственный компонет в ней).
    private JScrollPane scrollPane;
    // Панель со всей информацией (добавляется в скролл).
    private JPanel panelInfo;
    // Панель изображения с камеры.
    private GUIImagePanel panelImage;
    // Поля для отображения информации о файле.
    private JTextField textName;
    private JTextField textSize;
    private JTextField textType;
    private JTextField textCams;
    private JTextField textFirstTime;
    private JTextField textLastTime;
    private JTextField textAmountTime;
    // Текущая отображаемая информация о файле.
    private FileInfo curInfo;
    // Форматер для времени выводимого в инфе.
    private static SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    /**
     * Конструктор.
     */
    public GUIFileInfoPanel() {
        init();
    }

    /**
     * Воспомогательный метод для создания добавление метки+поля в инфу.
     * @param title Наименование в метке.
     * @param size Размер поля.
     * @param add Строка для MigLayout или null - если стандартно.
     * @return Сформированное поле.
     */
    private JTextField createInfo(String title, int size, String add) {
        JTextField tf = GUI.createText(size);
        panelInfo.add(GUI.createLabel(title));
        panelInfo.add(tf, add != null ? add + ",wrap" : "wrap");
        tf.setEditable(false);
        return tf;
    }

    /**
     * Инициализация графических компонентов.
     */
    private void init() {
        setLayout(new BorderLayout());
        //
        panelImage = new GUIImagePanel("<html><center>НЕТ<br></html>");
        JLabel l = panelImage.getLabel();
        l.setFont(new Font(l.getFont().getName(), Font.BOLD, 50));
        l.setForeground(new Color(0xA0A0B0));
        panelImage.setBackground(new Color(0x8080A0));
        setImageSize(dx, dy);
        panelImage.setBorder(new LineBorder(Color.red));
        panelImage.addMouseListener(new ImageMouseAdapter());
        panelImage.setToolTipText("Первый ключевой кадр.");
        panelImage.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        //
        panelInfo = new JPanel(new MigLayout("", "[]10[right][grow,shrink]"));
        panelInfo.add(panelImage, "id im1, spany");
        //
        textName = createInfo("Имя", 30, "growx");
        textSize = createInfo("Размер", 15, null);
        textType = createInfo("Тип", 10, null);
        textCams = createInfo("Камеры", 30, null);
        textFirstTime = createInfo("Начало", 15, null);
        textLastTime = createInfo("Конец", 15, null);
        textAmountTime = createInfo("Длительность", 22, null);
        panelInfo.add(GUI.createNoteLabel("Изменение масштаба происходит по нажатию кнопки мыши на кадре."), "pos im1.x2+10 im1.y2-pref");
        //
        scrollPane = new JScrollPane(panelInfo);
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Отображение инфы заданного файла.
     * @param info Инфа о файле.
     */
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
                panelImage.setImage(FFMpeg.getFirstFrameImage(info, App.srcCamSelect));
            }
        }
        curInfo = info;
    }

    /**
     * Конвертирует время (считая, что это длительность в мсек) в строку.
     * @param period Длительность в мсек.
     * @return Строка вида: * час. * мин. * сек. * мсек.
     */
    private String timeToString(long period) {
        long h = (period) / (3600 * 1000);
        long m = (period - h * 3600 * 1000) / (60 * 1000);
        long s = (period - h * 3600 * 1000 - m * 60 * 1000) / (1000);
        long ms = (period - h * 3600 * 1000 - m * 60 * 1000 - s * 1000);
        return String.format("%d час. %d мин. %d сек. %d мсек.", h, m, s, ms);
    }
    // Размер картинки - 1 - полкадра, 2 - полный кадр.
    private double zoom = 0.5;
    // Размер оригинальной картинки.
    private int dx = 2 * 352, dy = 2 * 288;

    public void setImageSize(int x, int y) {
        dx = x;
        dy = y;
        Dimension d = new Dimension((int) (dx * zoom) + 2, (int) (dy * zoom) + 2);
        panelImage.setPreferredSize(d);
        panelImage.setMinimumSize(d);
        panelImage.setMaximumSize(d);
        panelImage.setPreferredSize(d);
        panelImage.revalidate();
    }

    private class ImageMouseAdapter extends MouseAdapter {

        @Override
        public void mouseClicked(MouseEvent e) {
            // Меняем масштаб картинки.
            //App.log("Click! zoom="+zoom+" dx="+dx+" dy="+dy);
            zoom = (zoom == 1.0) ? 0.5 : 1.0;
            setImageSize(dx, dy);
        }
    }
}