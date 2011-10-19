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
import java.text.SimpleDateFormat;
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
    private JTextField textResolution;
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
        setImageSize();
        panelImage.setBorder(new LineBorder(Color.red));
        panelImage.addMouseListener(new ImageMouseAdapter());
        panelImage.setToolTipText("Первый ключевой кадр.");
        panelImage.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        //
        panelInfo = new JPanel(new MigLayout("", "[]10[right][grow,shrink]"));
        panelInfo.add(panelImage, "id im1, spany, top");
        //
        textName = createInfo("Имя", 30, "growx");
        textSize = createInfo("Размер", 15, null);
        textType = createInfo("Тип", 10, null);
        textCams = createInfo("Камеры", 30, null);
        textResolution = createInfo("Разрешение", 12, null);
        textFirstTime = createInfo("Начало", 15, null);
        textLastTime = createInfo("Конец", 15, null);
        textAmountTime = createInfo("Длительность", 22, null);
        panelInfo.add(GUI.createNoteLabel("Изменение масштаба происходит по нажатию кнопки мыши на кадре."),
                "spanx, pushy, bottom"); 
        //"pos im1.x2+10 im1.y2-pref"
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
            textResolution.setText("");
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
                Dimension d = info.frameFirst.getResolution();
                textResolution.setText(String.format("%d x %d", d.width, d.height));
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
        setImageSize();
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
    // Размер картинки: true - маленький (x=352,y-сжат), false - полный кадр.
    private boolean isSmallView = true;

    public void setImageSize() {
        int x = 704, y = 576;
        if (curInfo != null) {
            Dimension d = curInfo.frameFirst.getResolution();
            x = d.width;
            y = d.height;
        }
        if (isSmallView) {
            double z = 352.0 / x;
            x = (int)(z*x);
            y = (int)(z*y);
        }
        Dimension d = new Dimension(x + 2, y + 2);
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
            isSmallView = isSmallView ? false : true;
            setImageSize();
        }
    }
}
